package com.example.budongbudong.fixture;

import com.example.budongbudong.common.entity.*;

import java.math.BigDecimal;

public class BidFixture {

    public static Bid bid(User bidder, Auction auction) {
        return Bid.builder()
                .user(bidder)
                .auction(auction)
                .price(BigDecimal.valueOf(500000000))
                .build();
    }
}
