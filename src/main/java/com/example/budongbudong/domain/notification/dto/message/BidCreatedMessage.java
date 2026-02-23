package com.example.budongbudong.domain.notification.dto.message;

import com.example.budongbudong.domain.bid.event.BidCreatedEvent;

public record BidCreatedMessage(Long auctionId, Long bidderId) {
    public static BidCreatedMessage from(BidCreatedEvent event) {
        return new BidCreatedMessage(event.auctionId(), event.bidderId());
    }
}