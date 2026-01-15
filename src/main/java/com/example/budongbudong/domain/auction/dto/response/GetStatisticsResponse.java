package com.example.budongbudong.domain.auction.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import static com.example.budongbudong.common.util.TimeFormatUtil.formatTime;

@Getter
@RequiredArgsConstructor
public class GetStatisticsResponse {

    private final Long auctionId;
    private final int totalBidders;
    private final int totalBidCount;
    private final String lastBidTimeFormatted;
    private final long priceIncrease;
    private final LocalDateTime updatedAt;

    public static GetStatisticsResponse from(
            Long auctionId,
            int totalBidders,
            int totalBidCount,
            long priceIncrease,
            LocalDateTime updatedAt
    ) {
        return new GetStatisticsResponse(
                auctionId,
                totalBidders,
                totalBidCount,
                formatTime(updatedAt),
                priceIncrease,
                updatedAt
        );
    }
}
