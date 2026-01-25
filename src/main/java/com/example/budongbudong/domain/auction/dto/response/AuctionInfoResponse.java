package com.example.budongbudong.domain.auction.dto.response;

import com.example.budongbudong.common.entity.Auction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class AuctionInfoResponse {
    private final Long startPrice;
    private final Long minBidIncrement;
    private final Long highestPrice;
    private final int totalBidders;
    private final LocalDateTime startedAt;
    private final LocalDateTime endedAt;

    public static AuctionInfoResponse from(
            Auction auction,
            Long highestPrice,
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
