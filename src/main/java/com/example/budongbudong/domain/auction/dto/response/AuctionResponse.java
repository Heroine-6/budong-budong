package com.example.budongbudong.domain.auction.dto.response;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.property.search.document.AuctionSummary;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public class AuctionResponse {

    private final Long id;
    private final BigDecimal startPrice;
    private final AuctionStatus status;

    public static AuctionResponse from(Auction auction) {
        return new AuctionResponse(
                auction.getId(),
                auction.getStartPrice(),
                auction.getStatus()
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
                summary.getStatus()
        );
    }

}
