package com.example.budongbudong.domain.property.service;

import com.example.budongbudong.common.entity.*;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.property.document.AuctionSummary;
import com.example.budongbudong.domain.property.document.PropertySearchDocument;
import com.example.budongbudong.domain.property.repository.PropertyRepository;
import com.example.budongbudong.domain.property.repository.PropertySearchRepository;
import com.example.budongbudong.domain.propertyimage.repository.PropertyImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PropertySyncService {

    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository propertyImageRepository;
    private final AuctionRepository auctionRepository;
    private final ElasticsearchOperations operations;

    private static final int BATCH_SIZE = 1000;

    @Transactional(readOnly = true)
    public void syncAllProperties() {

        long lastId = 0L;

        while (true) {
            List<Property> properties = propertyRepository.findNextBatchNotDeleted(lastId, PageRequest.of(0, BATCH_SIZE));

            if (properties.isEmpty()) break;

            //property id 수집 - 경매를 N+1 방지하며 한번에 조회하기 위함
            List<Long> propertyIds = properties.stream().map(Property::getId).toList();

            // propertyIds를 기준으로 해당 매물들의 경매 정보 한번의 쿼리로 조회
            Map<Long, Auction> auctionMap =
                    auctionRepository.findAllByPropertyIds(propertyIds).stream()
                            .collect(Collectors.toMap(
                                    a -> a.getProperty().getId(),
                                    a -> a,
                                    (a, b) -> a
                            ));

            Map<Long, String> thumbnailMap =
                    propertyImageRepository.findThumbnailImagesByPropertyIds(propertyIds).stream()
                            .collect(Collectors.toMap(
                                    img -> img.getProperty().getId(),
                                    PropertyImage::getImageUrl,
                                    (a, b) -> a
                            ));
            // document 변환
            List<PropertySearchDocument> docs = properties.stream()
                    .map(p -> toDocument(p, auctionMap.get(p.getId()), thumbnailMap.get(p.getId()))).toList();

            operations.save(docs);

            lastId = properties.get(properties.size() - 1).getId();
        }
    }

    private PropertySearchDocument toDocument(Property property, Auction auction, String thumbnailUrl) {

        return PropertySearchDocument.builder()
                .id(property.getId())
                .name(property.getName())
                .address(property.getAddress())
                .type(property.getType())
                .description(property.getDescription())
                .supplyArea(property.getSupplyArea())
                .privateArea(property.getPrivateArea())
                .price(property.getPrice())
                .builtYear(property.getBuiltYear().getValue())
                .thumbnailImage(thumbnailUrl)
                .auction(auction != null ? AuctionSummary.from(auction) : null)
                .build();
    }
}

