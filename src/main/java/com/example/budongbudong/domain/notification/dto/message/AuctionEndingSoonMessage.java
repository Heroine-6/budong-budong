package com.example.budongbudong.domain.notification.dto.message;

import com.example.budongbudong.domain.auction.event.AuctionEndingSoonEvent;

public record AuctionEndingSoonMessage(Long auctionId) {
    public static AuctionEndingSoonMessage from(AuctionEndingSoonEvent event) {
        return new AuctionEndingSoonMessage(event.auctionId());
    }
}

