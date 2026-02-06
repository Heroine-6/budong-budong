package com.example.budongbudong.domain.notification.dto.message;

import com.example.budongbudong.domain.auction.event.AuctionCreatedEvent;

public record AuctionCreatedMessage(Long auctionId, Long sellerId) {
    public static AuctionCreatedMessage from(AuctionCreatedEvent event) {
        return new AuctionCreatedMessage(event.auctionId(), event.sellerId());
    }
}