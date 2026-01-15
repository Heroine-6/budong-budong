package com.example.budongbudong.domain.auction.dto.request;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CreateAuctionRequest {

    private Long propertyId;
    private Long startPrice;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

}
