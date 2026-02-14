package com.example.budongbudong.domain.auction.dto.response;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.auction.enums.AuctionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AuctionInfoResponse(
        AuctionType type,
        AuctionStatus status,
        Long propertyId,
        BigDecimal startPrice,
        BigDecimal minBidIncrement,
        BigDecimal highestPrice,
        int totalBidders,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        BigDecimal currentPrice,
        BigDecimal endPrice,
        BigDecimal decreasePrice
) {
    public static AuctionInfoResponse from(
            Auction auction,
            BigDecimal highestPrice,
            int totalBidders
    ) {
        return new AuctionInfoResponse(
                auction.getType(),
                auction.getStatus(),
                auction.getProperty().getId(),
                auction.getStartPrice(),
                auction.getMinBidIncrement(),
                highestPrice,
                totalBidders,
                auction.getStartedAt(),
                auction.getEndedAt(),
                auction.getCurrentPrice(),
                auction.getEndPrice(),
                auction.getDecreasePrice()
        );
    }
}
