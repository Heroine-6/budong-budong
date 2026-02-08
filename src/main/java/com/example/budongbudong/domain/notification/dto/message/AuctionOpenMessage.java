package com.example.budongbudong.domain.notification.dto.message;

import com.example.budongbudong.domain.auction.event.AuctionOpenEvent;

public record AuctionOpenMessage(Long auctionId) {
    public static AuctionOpenMessage from(AuctionOpenEvent event) {
        return new AuctionOpenMessage(event.auctionId());
    }
}