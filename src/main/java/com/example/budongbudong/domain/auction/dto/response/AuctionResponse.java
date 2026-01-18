package com.example.budongbudong.domain.auction.dto.response;

import com.example.budongbudong.domain.auction.entity.Auction;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class AuctionResponse {

    private final Long id;
    private final Long startPrice;
    private final AuctionStatus status;
    private final LocalDateTime startedAt;
    private final LocalDateTime endedAt;

    public static AuctionResponse from(Auction auction) {
        return new AuctionResponse(
                auction.getId(),
                auction.getStartPrice(),
                auction.getStatus(),
                auction.getStartedAt(),
                auction.getEndedAt()
        );
    }
}
