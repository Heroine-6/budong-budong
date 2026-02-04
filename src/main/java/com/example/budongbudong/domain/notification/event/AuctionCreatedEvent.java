package com.example.budongbudong.domain.notification.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuctionCreatedEvent {

    private final Long auctionId;
    private final Long sellerId;
}