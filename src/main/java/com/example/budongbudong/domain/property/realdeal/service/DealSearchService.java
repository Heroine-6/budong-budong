package com.example.budongbudong.domain.property.realdeal.service;

import co.elastic.clients.elasticsearch._types.GeoDistanceType;
import co.elastic.clients.elasticsearch._types.ScriptSortType;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.domain.property.enums.PropertyType;
import com.example.budongbudong.domain.property.realdeal.enums.DealSortType;
import com.example.budongbudong.domain.property.realdeal.client.KakaoGeoClient;
import com.example.budongbudong.domain.property.realdeal.client.KakaoGeoResponse;
import com.example.budongbudong.domain.property.realdeal.client.NaverGeoClient;
import com.example.budongbudong.domain.property.realdeal.client.NaverGeoResponse;
import com.example.budongbudong.domain.property.realdeal.document.RealDealDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 실거래가 검색 서비스
 * - 주소 → 지오코딩 → 좌표 기반 반경 검색 (ES geo_distance)
 * - 지오코딩: 네이버 우선, 카카오 fallback
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DealSearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final NaverGeoClient naverGeoClient;
    private final KakaoGeoClient kakaoGeoClient;

    /**
     * 특정 좌표 기준 반경 내 실거래 데이터 조회
     * @param lat 위도
     * @param lon 경도
     * @param distanceKm 반경 (km)
     * @param size 조회 건수
     * @return 거리순 정렬된 실거래 목록
     */
    public SearchHits<RealDealDocument> findNearby(double lat, double lon, double distanceKm, int size) {
        return findNearby(lat, lon, distanceKm, size, null, null, null, null, null, DealSortType.DISTANCE);
    }

    public SearchHits<RealDealDocument> findNearby(double lat, double lon, double distanceKm, int size,
                                             BigDecimal minArea, BigDecimal maxArea,
                                             BigDecimal minPrice, BigDecimal maxPrice,
                                             PropertyType propertyType, DealSortType sortType) {
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        boolBuilder.filter(f -> f
                .geoDistance(g -> g
                        .field("location")
                        .distance(distanceKm + "km")
                        .location(loc -> loc.latlon(ll -> ll.lat(lat).lon(lon)))
                        .distanceType(GeoDistanceType.Arc)
                )
        );

        if (propertyType != null) {
            boolBuilder.filter(f -> f
                    .term(t -> t.field("propertyType").value(propertyType.name()))
            );
        }

        if (minArea != null || maxArea != null) {
            boolBuilder.filter(f -> f
                    .range(r -> r
                            .number(n -> {
                                n.field("exclusiveArea");
                                if (minArea != null) n.gte(minArea.doubleValue());
                                if (maxArea != null) n.lte(maxArea.doubleValue());
                                return n;
                            })
                    )
            );
        }

        if (minPrice != null || maxPrice != null) {
            boolBuilder.filter(f -> f
                    .range(r -> r
                            .number(n -> {
                                n.field("dealAmount");
                                if (minPrice != null) n.gte(minPrice.doubleValue());
                                if (maxPrice != null) n.lte(maxPrice.doubleValue());
                                return n;
                            })
                    )
            );
        }

        NativeQueryBuilder queryBuilder = NativeQuery.builder()
                .withQuery(q -> q.bool(boolBuilder.build()))
                .withPageable(PageRequest.of(0, size));

        if (sortType == DealSortType.PRICE_PER_AREA_ASC || sortType == DealSortType.PRICE_PER_AREA_DESC) {
            SortOrder order = sortType == DealSortType.PRICE_PER_AREA_ASC ? SortOrder.Asc : SortOrder.Desc;
            String fallback = sortType == DealSortType.PRICE_PER_AREA_ASC ? "Long.MAX_VALUE" : "0";
            String scriptSource = "doc['exclusiveArea'].size()==0 || doc['exclusiveArea'].value==0 ? "
                    + fallback + " : doc['dealAmount'].value / doc['exclusiveArea'].value";
            queryBuilder.withSort(s -> s
                    .script(sc -> sc
                            .type(ScriptSortType.Number)
                            .script(script -> script.source(scriptSource))
                            .order(order)
                    )
            );
        } else {
            queryBuilder.withSort(s -> s
                    .geoDistance(g -> g
                            .field("location")
                            .location(loc -> loc.latlon(ll -> ll.lat(lat).lon(lon)))
                            .order(SortOrder.Asc)
                    )
            );
        }

        return elasticsearchOperations.search(queryBuilder.build(), RealDealDocument.class);
    }

    /**
     * 특정 좌표 기준 반경 1km 내 실거래 데이터 조회 (기본값)
     */
    public SearchHits<RealDealDocument> findNearby(double lat, double lon) {
        return findNearby(lat, lon, 1.0, 100);
    }

    /**
     * 주소 입력으로 실거래 데이터 검색
     * - 네이버 지오코딩 → 카카오 fallback → 좌표 기반 반경 검색
     *
     * @param address 검색할 주소
     * @param distanceKm 반경 (km)
     * @param size 조회 건수
     * @return 반경 내 실거래 목록
     */
    public SearchHits<RealDealDocument> findByAddress(String address, double distanceKm, int size,
                                                BigDecimal minArea, BigDecimal maxArea,
                                                BigDecimal minPrice, BigDecimal maxPrice,
                                                PropertyType propertyType, DealSortType sortType) {
        double[] coords = geocode(address);
        double lat = coords[0];
        double lon = coords[1];

        log.info("[주소 검색] {} → 좌표({}, {}) → 반경 {}km 검색", address, lat, lon, distanceKm);
        return findNearby(lat, lon, distanceKm, size, minArea, maxArea, minPrice, maxPrice, propertyType, sortType);
    }

    /**
     * 주소 입력으로 실거래 데이터 검색 (기본값: 1km, 100건)
     */
    public SearchHits<RealDealDocument> findByAddress(String address) {
        return findByAddress(address, 1.0, 100, null, null, null, null, null, DealSortType.DISTANCE);
    }

    /**
     * 주소 → 좌표 변환 (네이버 우선, 카카오 fallback)
     * @return [lat, lon]
     */
    private double[] geocode(String address) {
        // 네이버 지오코딩 시도
        try {
            NaverGeoResponse naverResponse = naverGeoClient.geocode(address);
            if (naverResponse.hasResult()) {
                NaverGeoResponse.Address addr = naverResponse.addresses().get(0);
                log.info("[지오코딩] 네이버 성공: {}", address);
                return new double[]{Double.parseDouble(addr.y()), Double.parseDouble(addr.x())};
            }
        } catch (Exception e) {
            log.warn("[지오코딩] 네이버 실패: {} - {}", address, e.getMessage());
        }

        // 카카오 fallback
        try {
            KakaoGeoResponse kakaoResponse = kakaoGeoClient.geocode(address);
            if (kakaoResponse.hasResult()) {
                KakaoGeoResponse.Document doc = kakaoResponse.documents().get(0);
                log.info("[지오코딩] 카카오 성공: {}", address);
                return new double[]{Double.parseDouble(doc.y()), Double.parseDouble(doc.x())};
            }
        } catch (Exception e) {
            log.warn("[지오코딩] 카카오 실패: {} - {}", address, e.getMessage());
        }

        throw new CustomException(ErrorCode.GEOCODING_FAILED);
    }
}
