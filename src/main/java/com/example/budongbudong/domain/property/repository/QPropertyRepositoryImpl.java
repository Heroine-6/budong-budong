package com.example.budongbudong.domain.property.repository;

import com.example.budongbudong.domain.auction.dto.response.AuctionResponse;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.property.dto.QReadAllPropertyDto;
import com.example.budongbudong.domain.property.dto.ReadAllPropertyDto;
import com.example.budongbudong.domain.property.dto.condition.SearchPropertyCond;
import com.example.budongbudong.domain.property.dto.response.ReadAllPropertyResponse;
import com.example.budongbudong.domain.property.enums.PropertyType;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.Year;
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
     * 매물 목록 검색
     * - 대표 이미지(1장), 경매 정보까지 함께 조회, 검색
     * - Querydsl DTO Projection 기반
     */
    @Override
    public Page<ReadAllPropertyResponse> searchProperties(SearchPropertyCond cond, Pageable pageable) {

        BooleanExpression baseCondition = property.isDeleted.isFalse();
        BooleanExpression auctionExistsWhenStatus = cond.getStatus() != null ? auction.id.isNotNull():null;
        BooleanExpression[] condList = {
                baseCondition,
                nameContains(cond.getName()),
                typeEq(cond.getType()),
                addressContains(cond.getAddress()),
                minPriceGoe(cond.getMinPrice()),
                maxPriceLoe(cond.getMaxPrice()),
                migrateDateGoeFrom(cond.getMigrateDate()),
                builtYearFrom(cond.getBuiltYear()),
                auctionExistsWhenStatus
        };

        BooleanExpression joinCondition = auction.property.id.eq(property.id);
        if (cond.getStatus() != null) {
            joinCondition = joinCondition.and(auction.status.eq(cond.getStatus()));
        }

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
                .leftJoin(auction)
                .on(joinCondition)
                .where(condList)
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
                        .leftJoin(auction).on(joinCondition)
                        .where(condList)
                        .fetchFirst();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression nameContains(String name) {
        return StringUtils.hasText(name) ? property.name.contains(name) : null;
    }

    private BooleanExpression typeEq(PropertyType type) {
        return type != null ? property.type.eq(type) : null;
    }

    private BooleanExpression addressContains(String address) {
        return StringUtils.hasText(address) ?  property.address.contains(address) : null;
    }

    private BooleanExpression minPriceGoe(Long minPrice) {
        return minPrice != null ? property.price.goe(minPrice) : null;
    }

    private BooleanExpression maxPriceLoe(Long maxPrice) {
        return maxPrice != null ? property.price.loe(maxPrice) : null;
    }

    private BooleanExpression migrateDateGoeFrom(LocalDate migrateDate) {
        return migrateDate != null ? property.migrateDate.goe(migrateDate) : null;
    }

    private BooleanExpression builtYearFrom (Year builtYear) {
        return builtYear != null ? property.builtYear.goe(builtYear) : null;
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
                        auction.startedAt,
                        auction.endedAt,
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
                dto.getStatus(),
                dto.getStartedAt(),
                dto.getEndedAt()
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
}
