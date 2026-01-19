package com.example.budongbudong.domain.auction.dto.response;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class CancelAuctionResponse {

    private final Long id;
    private final Long propertyId;
    private final Long startPrice;
    private final AuctionStatus status;
    private final LocalDateTime startedAt;
    private final LocalDateTime endedAt;
    private final LocalDateTime createdAt;

    public static CancelAuctionResponse from(Auction auction) {
        return new CancelAuctionResponse(
                auction.getId(),
                auction.getProperty().getId(),
                auction.getStartPrice(),
                auction.getStatus(),
                auction.getStartedAt(),
                auction.getEndedAt(),
                auction.getCreatedAt()
        );
    }
}
