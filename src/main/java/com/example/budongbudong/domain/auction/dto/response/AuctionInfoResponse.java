package com.example.budongbudong.domain.auction.dto.response;

import java.time.LocalDateTime;

public record AuctionInfoResponse(
        Long startPrice,
        Long highestPrice,
        Long totalBidders,
        LocalDateTime endedAt
) {
}
