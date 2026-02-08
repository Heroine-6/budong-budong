package com.example.budongbudong.domain.auction.dto.response;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.property.search.document.AuctionSummary;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class AuctionResponse {

    private final Long id;
    private final BigDecimal startPrice;
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

    /**
     * Elasticsearch용 팩토리 메서드
     */
    public static AuctionResponse from(AuctionSummary summary) {
        if (summary == null) {
            return null;
        }

        return new AuctionResponse(
                summary.getAuctionId(),
                summary.getStartPrice(),
                summary.getStatus(),
                summary.getStartedAt(),
                summary.getEndedAt()
        );
    }

}
