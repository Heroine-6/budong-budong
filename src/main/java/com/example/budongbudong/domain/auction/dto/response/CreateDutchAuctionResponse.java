package com.example.budongbudong.domain.auction.dto.response;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class CreateDutchAuctionResponse {

    private final Long id;
    private final Long propertyId;
    private final BigDecimal startPrice;
    private final BigDecimal endPrice;
    private final BigDecimal decreasePrice;
    private final AuctionStatus status;
    private final LocalDateTime startedAt;
    private final LocalDateTime endedAt;
    private final LocalDateTime createdAt;

    public static CreateDutchAuctionResponse from(Auction auction) {
        return new CreateDutchAuctionResponse(
                auction.getId(),
                auction.getProperty().getId(),
                auction.getStartPrice(),
                auction.getEndPrice(),
                auction.getDecreasePrice(),
                auction.getStatus(),
                auction.getStartedAt(),
                auction.getEndedAt(),
                auction.getCreatedAt()
        );
    }
}
