package com.example.budongbudong.domain.property.repository;

import com.example.budongbudong.domain.auction.dto.response.AuctionResponse;
import com.example.budongbudong.domain.property.dto.QReadAllPropertyDto;
import com.example.budongbudong.domain.property.dto.ReadAllPropertyDto;
import com.example.budongbudong.domain.property.dto.response.ReadAllPropertyResponse;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.budongbudong.common.entity.QAuction.auction;
import static com.example.budongbudong.common.entity.QProperty.property;
import static com.example.budongbudong.common.entity.QPropertyImage.propertyImage;


@Repository
@RequiredArgsConstructor
public class QPropertyRepositoryImpl implements QPropertyRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 매물 목록을 페이징 조회
     * - 대표 이미지(1장), 경매 정보까지 함께 조회
     * - Querydsl DTO Projection 기반
     */
    @Override
    public Page<ReadAllPropertyResponse> findAllProperties(Pageable pageable) {

        BooleanExpression baseCondition = property.isDeleted.isFalse();
        var thumbnailSubquery =
                JPAExpressions
                        .select(propertyImage.imageUrl)
                        .from(propertyImage)
                        .where(propertyImage.id.eq(
                                JPAExpressions
                                        .select(propertyImage.id.min())
                                        .from(propertyImage)
                                        .where(
                                                propertyImage.property.id.eq(property.id),
                                                propertyImage.isDeleted.isFalse()
                                        )
                        ));

        List<ReadAllPropertyDto> results = queryFactory
                .select(new QReadAllPropertyDto(
                        property.id,
                        property.name,
                        property.address,
                        property.type,
                        property.description,
                        property.supplyArea,
                        property.privateArea,
                        auction.id,
                        auction.startPrice,
                        auction.status,
                        auction.startedAt,
                        auction.endedAt,
                        thumbnailSubquery
                ))
                .from(property)
                .leftJoin(auction).on(auction.property.id.eq(property.id))
                .where(baseCondition)
                .orderBy(property.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<ReadAllPropertyResponse> content = results.stream()
                .map(this::toResponse)
                .toList();

        long total =
                queryFactory
                        .select(property.count())
                        .from(property)
                        .where(baseCondition)
                        .fetchFirst();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Querydsl 조회 DTO를 API 응답 DTO로 변환한다.
     * - 경매 정보는 존재할 경우에만 포함
     */
    private ReadAllPropertyResponse toResponse(ReadAllPropertyDto dto) {
        AuctionResponse response = dto.getAuctionId() != null
                ? new AuctionResponse(
                dto.getAuctionId(),
                dto.getStartPrice(),
                dto.getStatus(),
                dto.getStartedAt(),
                dto.getEndedAt()
                )
                :null;

        return new ReadAllPropertyResponse(
                dto.getPropertyId(),
                dto.getName(),
                dto.getAddress(),
                dto.getType(),
                dto.getDescription(),
                dto.getSupplyArea(),
                dto.getPrivateArea(),
                dto.getThumbnailUrl(),
                response
        );
    }
}
