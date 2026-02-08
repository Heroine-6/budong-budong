package com.example.budongbudong.domain.property.search;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.Property;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.property.search.document.AuctionSummary;
import com.example.budongbudong.domain.property.search.document.PropertySearchDocument;
import com.example.budongbudong.domain.property.repository.PropertyRepository;
import com.example.budongbudong.domain.property.repository.PropertySearchRepository;
import com.example.budongbudong.domain.propertyimage.repository.PropertyImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.*;
import org.springframework.stereotype.Component;

import java.time.Year;

/**
 * Elasticsearch 인덱스 갱신을 책임지는 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PropertyIndexer {

    private final PropertyRepository propertyRepository;
    private final PropertySearchRepository propertySearchRepository;
    private final PropertyImageRepository propertyImageRepository;
    private final AuctionRepository auctionRepository;

    @Retryable(retryFor = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 200, multiplier = 2))
    public void insertUpdate(Long propertyId) {

        Property property = propertyRepository.getByIdAndNotDeletedOrThrow(propertyId);
        Auction auction= auctionRepository.findByPropertyIdOrThrowIfExists(propertyId);

        String thumbnailImage = propertyImageRepository.findThumbnailImageUrl(propertyId);

        Year builtYear = property.getBuiltYear();
        Integer builtYearValue = builtYear.getValue();

        PropertySearchDocument document = PropertySearchDocument.builder()
                        .id(property.getId())
                        .name(property.getName())
                        .address(property.getAddress())
                        .type(property.getType())
                        .builtYear(builtYearValue)
                        .migrateDate(property.getMigrateDate())

                        // 응답 전용 필드 (검색/필터 대상 아님)
                        .thumbnailImage(thumbnailImage)

                        // 검색/정렬용 필드
                        .price(property.getPrice())

                        // 경매 요약 (없으면 null)
                        .auction(AuctionSummary.from(auction))
                        .build();
        propertySearchRepository.save(document);
    }

    @Retryable(retryFor = { Exception.class }, maxAttempts = 3, backoff = @Backoff(delay = 200, multiplier = 2))
    public void delete(Long propertyId) {

        propertySearchRepository.deleteById(propertyId);
    }

    /**
     * 재시도까지 실패한 경우
     * - /sync API 호출이 필요한 재동기화 대상임을 로그로 남긴다
     */
    @Recover
    public void recover(Exception e, Long propertyId) {
        log.error("[매물 ES 동기화 실패] propertyId={} 동기화 API 호출 필요", propertyId, e);
    }

}
