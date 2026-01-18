package com.example.budongbudong.domain.property.dto;

import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.property.enums.PropertyType;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * QueryDSL 조회 전용 DTO
 * */
@Getter
public class ReadAllPropertyDto {
    private final Long propertyId;
    private final String name;
    private final String address;
    private final PropertyType type;
    private final String description;
    private final BigDecimal supplyArea;
    private final BigDecimal privateArea;
    private final Long auctionId;
    private final Long startPrice;
    private final AuctionStatus status;
    private final LocalDateTime startedAt;
    private final LocalDateTime endedAt;
    private final String thumbnailUrl;

    /**
     * Querydsl Projection 생성자
     *
     * <p>
     * {@link QueryProjection}을 사용해 Q클래스를 생성하며,
     * Querydsl 조회 결과를 타입 안정성 있게 매핑하기 위한 생성자이다.
     * </p>
     * */
    @QueryProjection
    public ReadAllPropertyDto(
            Long propertyId,
            String name,
            String address,
            PropertyType type,
            String description,
            BigDecimal supplyArea,
            BigDecimal privateArea,
            Long auctionId,
            Long startPrice,
            AuctionStatus status,
            LocalDateTime startedAt,
            LocalDateTime endedAt,
            String thumbnailUrl

    ) {
        this.propertyId = propertyId;
        this.name = name;
        this.address = address;
        this.type = type;
        this.description = description;
        this.supplyArea = supplyArea;
        this.privateArea = privateArea;
        this.auctionId = auctionId;
        this.startPrice = startPrice;
        this.status = status;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.thumbnailUrl = thumbnailUrl;
    }
}
