package com.example.budongbudong.domain.auction.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import static com.example.budongbudong.common.utils.TimeFormatUtil.formatTime;

@Getter
@RequiredArgsConstructor
public class GetStatisticsResponse {

    private final Long auctionId;
    private final int totalBidders;
    private final int totalBidCount;
    private final long priceIncrease;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String lastBidTimeFormatted;
    @JsonInclude(JsonInclude.Include.NON_NULL)
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
                priceIncrease,
                formatTime(updatedAt),
                updatedAt
        );
    }
}
