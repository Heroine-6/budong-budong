package com.example.budongbudong.domain.bid.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBidMessageRequest {
    private Long auctionId;
    private Long userId;
    private CreateBidRequest createBidRequest;
    private LocalDateTime bidAt;
    private int retryCount;

    public static CreateBidMessageRequest from(
            Long auctionId,
            Long userId,
            CreateBidRequest createBidRequest
    ) {
        return new CreateBidMessageRequest(
                auctionId,
                userId,
                createBidRequest,
                LocalDateTime.now(),
                0
        );
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }
}
