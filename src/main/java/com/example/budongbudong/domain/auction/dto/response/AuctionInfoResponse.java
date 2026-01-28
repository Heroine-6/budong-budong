package com.example.budongbudong.domain.auction.dto.response;

import com.example.budongbudong.common.entity.Auction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class AuctionInfoResponse {
    private final BigDecimal startPrice;
    private final BigDecimal minBidIncrement;
    private final BigDecimal highestPrice;
    private final int totalBidders;
    private final LocalDateTime startedAt;
    private final LocalDateTime endedAt;

    public static AuctionInfoResponse from(
            Auction auction,
            BigDecimal highestPrice,
            int totalBidders
    ) {
        return new AuctionInfoResponse(
                auction.getStartPrice(),
                auction.getMinBidIncrement(),
                highestPrice,
                totalBidders,
                auction.getStartedAt(),
                auction.getEndedAt()
        );
    }
}
