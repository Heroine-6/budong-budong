package com.example.budongbudong.domain.auction.dto;

import com.example.budongbudong.domain.auction.entity.Auction;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class AuctionResponse {

    private final Long id;
    private final Long startPrice;
    private final LocalDateTime startedAt;
    private final LocalDateTime endedAt;

    public static AuctionResponse from(Auction auction) {
        return new AuctionResponse(
                auction.getId(),
                auction.getStartPrice(),
                auction.getStartedAt(),
                auction.getEndedAt()
        );
    }
}
