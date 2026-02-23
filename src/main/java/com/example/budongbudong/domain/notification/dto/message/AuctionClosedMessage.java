package com.example.budongbudong.domain.notification.dto.message;

import com.example.budongbudong.domain.auction.event.AuctionClosedEvent;

public record AuctionClosedMessage(Long auctionId) {
    public static AuctionClosedMessage from(AuctionClosedEvent event) {
        return new AuctionClosedMessage(event.auctionId());
    }
}