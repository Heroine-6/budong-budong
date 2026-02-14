package com.example.budongbudong.domain.property.dto.response;

import com.example.budongbudong.common.entity.*;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.auction.enums.AuctionType;
import com.example.budongbudong.domain.property.enums.PropertyType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ReadAuctionPropertyResponse {

    //매물
    private final String name;
    private final String address;
    private final int floor;
    private final int totalFloor;
    private final int roomCount;
    private final BigDecimal privateArea;
    private final BigDecimal supplyArea;
    private final Year builtYear;
    private final String description;
    private final PropertyType propertyType;
    private final List<String> images;

    //경매
    private final Long auctionId;
    private final AuctionStatus status;
    private final AuctionType auctionType;
    private final BigDecimal startPrice;
    private final BigDecimal currentPrice;
    private final BigDecimal minBidIncrement;
    private final BigDecimal decreasePrice;
    private final LocalDateTime startedAt;
    private final LocalDateTime endedAt;

    public static ReadAuctionPropertyResponse from(Property property, Auction auction) {

        return new ReadAuctionPropertyResponse(
                property.getName(),
                property.getAddress(),
                property.getFloor(),
                property.getTotalFloor(),
                property.getRoomCount(),
                property.getPrivateArea(),
                property.getSupplyArea(),
                property.getBuiltYear(),
                property.getDescription(),
                property.getType(),
                property.getPropertyImageList()
                        .stream()
                        .map(PropertyImage::getImageUrl)
                        .toList(),

                auction.getId(),
                auction.getStatus(),
                auction.getType(),
                auction.getStartPrice(),
                auction.getCurrentPrice(),
                auction.getMinBidIncrement(),
                auction.getDecreasePrice(),
                auction.getStartedAt(),
                auction.getEndedAt()
        );
    }
}

