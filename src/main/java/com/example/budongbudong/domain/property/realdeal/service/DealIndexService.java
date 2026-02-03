package com.example.budongbudong.domain.property.realdeal.service;

import com.example.budongbudong.common.entity.RealDeal;
import com.example.budongbudong.domain.property.realdeal.document.RealDealDocument;
import com.example.budongbudong.domain.property.realdeal.repository.RealDealRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Elasticsearch 인덱싱 서비스
 *
 * - 지오코딩 완료된 실거래가 데이터를 ES에 인덱싱
 * - geo_point 타입으로 저장하여 위치 기반 검색 지원
 * - 커서 기반 페이징으로 대용량 처리
 * - 실패 시 자동 재시도 (3회, 지수 백오프)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DealIndexService {

    private final RealDealRepository realDealRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    private static final int BATCH_SIZE = 1000;  // ES bulk insert 단위

    @Transactional(readOnly = true)
    public int indexAll() {
        long lastId = 0L;
        int totalIndexed = 0;

        while (true) {
            List<RealDeal> batch = realDealRepository.findGeoCodedAfter(
                    lastId, PageRequest.of(0, BATCH_SIZE)
            );
            if (batch.isEmpty()) break;

            List<RealDealDocument> docs = batch.stream()
                    .map(this::toDocument)
                    .toList();

            indexBatch(docs);
            totalIndexed += docs.size();
            lastId = batch.get(batch.size() - 1).getId();
        }

        return totalIndexed;
    }

    @Retryable(retryFor = {Exception.class}, maxAttempts = 3,
            backoff = @Backoff(delay = 200, multiplier = 2))
    public void indexBatch(List<RealDealDocument> docs) {
        elasticsearchOperations.save(docs);
    }

    private RealDealDocument toDocument(RealDeal deal) {
        return RealDealDocument.builder()
                .id(deal.getId())
                .propertyName(deal.getPropertyName())
                .address(deal.getAddress())
                .roadAddress(deal.getRoadAddress())
                .dealAmount(deal.getDealAmount())
                .exclusiveArea(deal.getExclusiveArea())
                .floor(deal.getFloor())
                .builtYear(deal.getBuiltYear())
                .dealDate(deal.getDealDate())
                .propertyType(deal.getPropertyType())
                .location(new GeoPoint(
                        deal.getLatitude().doubleValue(),
                        deal.getLongitude().doubleValue()
                ))
                .build();
    }
}
