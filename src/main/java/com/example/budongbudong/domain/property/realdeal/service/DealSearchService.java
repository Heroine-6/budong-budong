package com.example.budongbudong.domain.property.realdeal.service;

import co.elastic.clients.elasticsearch._types.GeoDistanceType;
import co.elastic.clients.elasticsearch._types.SortOrder;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.domain.property.realdeal.client.KakaoGeoClient;
import com.example.budongbudong.domain.property.realdeal.client.KakaoGeoResponse;
import com.example.budongbudong.domain.property.realdeal.client.NaverGeoClient;
import com.example.budongbudong.domain.property.realdeal.client.NaverGeoResponse;
import com.example.budongbudong.domain.property.realdeal.document.RealDealDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

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
    public List<RealDealDocument> findNearby(double lat, double lon, double distanceKm, int size) {
        Query query = NativeQuery.builder()
                .withQuery(q -> q
                        .geoDistance(g -> g
                                .field("location")
                                .distance(distanceKm + "km")
                                .location(loc -> loc.latlon(ll -> ll.lat(lat).lon(lon)))
                                .distanceType(GeoDistanceType.Arc)
                        )
                )
                .withSort(s -> s
                        .geoDistance(g -> g
                                .field("location")
                                .location(loc -> loc.latlon(ll -> ll.lat(lat).lon(lon)))
                                .order(SortOrder.Asc)
                        )
                )
                .withPageable(PageRequest.of(0, size))
                .build();

        SearchHits<RealDealDocument> hits = elasticsearchOperations.search(query, RealDealDocument.class);

        return hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();
    }

    /**
     * 특정 좌표 기준 반경 1km 내 실거래 데이터 조회 (기본값)
     */
    public List<RealDealDocument> findNearby(double lat, double lon) {
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
    public List<RealDealDocument> findByAddress(String address, double distanceKm, int size) {
        double[] coords = geocode(address);
        double lat = coords[0];
        double lon = coords[1];

        log.info("[주소 검색] {} → 좌표({}, {}) → 반경 {}km 검색", address, lat, lon, distanceKm);
        return findNearby(lat, lon, distanceKm, size);
    }

    /**
     * 주소 입력으로 실거래 데이터 검색 (기본값: 1km, 100건)
     */
    public List<RealDealDocument> findByAddress(String address) {
        return findByAddress(address, 1.0, 100);
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
