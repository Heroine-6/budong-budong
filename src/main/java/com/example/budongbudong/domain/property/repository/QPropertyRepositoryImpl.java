package com.example.budongbudong.domain.property.repository;

import com.example.budongbudong.common.entity.*;
import com.example.budongbudong.domain.auction.dto.response.AuctionResponse;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.property.dto.QReadAllPropertyDto;
import com.example.budongbudong.domain.property.dto.ReadAllPropertyDto;
import com.example.budongbudong.domain.property.dto.response.ReadAllPropertyResponse;
import com.example.budongbudong.domain.property.enums.PropertyType;
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
import static com.example.budongbudong.common.entity.QUser.user;


@Repository
@RequiredArgsConstructor
public class QPropertyRepositoryImpl implements QPropertyRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 매물 목록 조회 (메인/탐색 화면용)
     * - 단순 필터 기반
     * - 대표 이미지 1장
     * - 경매 정보는 존재 여부만
     * - Slice 기반 페이징 (count 쿼리 없음)
     */
    @Override
    public Slice<ReadAllPropertyResponse> findPropertyList(
            PropertyType type,
            AuctionStatus auctionStatus,
            Pageable pageable
    ) {

        int pageSize = pageable.getPageSize();
        int requestSize = pageSize + 1; // 다음 페이지 존재 여부 판단을 위해 +1 조회

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

        //Property Slice 조회
        List<ReadAllPropertyDto> results =
                queryFactory
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
                                thumbnailSubquery
                        ))
                        .from(property)
                        .leftJoin(auction).on(
                                auction.property.id.eq(property.id),
                                auction.isDeleted.isFalse()
                        )
                        .where(
                                property.isDeleted.isFalse(),
                                typeEq(type),
                                auctionStatusEq(auctionStatus)
                        )
                        .orderBy(property.createdAt.desc())
                        .offset(pageable.getOffset())
                        .limit(requestSize)
                        .fetch();

        //Slice의 다음 페이지 존재 여부 판단
        boolean hasNext = results.size() > pageSize;
        if (hasNext) {
            results = results.subList(0, pageSize);
        }

        if (results.isEmpty()) {
            return new SliceImpl<>(List.of(), pageable, false);
        }

        //DTO 변환
        List<ReadAllPropertyResponse> content = results.stream()
                .map(this::toResponse)
                .toList();

        return new SliceImpl<>(content, pageable, hasNext);
    }

    /**
     * 내 매물 목록 페이징 조회
     */
    @Override
    public Page<ReadAllPropertyResponse> findAllMyProperties(Long userId, Pageable pageable) {

        // 매물 소프트 딜리트 검증
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
                        thumbnailSubquery
                ))
                .from(property)
                .leftJoin(auction).on(auction.property.id.eq(property.id))
                .where(
                        baseCondition,
                        property.user.id.eq(userId),
                        user.isDeleted.isFalse()
                )
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
                        .where(
                                baseCondition,
                                property.user.id.eq(userId),
                                user.isDeleted.isFalse()
                        )
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
                dto.getStatus()
        )
                : null;

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

    private BooleanExpression typeEq(PropertyType type) {
        return type != null ? property.type.eq(type) : null;
    }

    private BooleanExpression auctionStatusEq(AuctionStatus status) {
        return status != null ? auction.status.eq(status) : null;
    }
}
