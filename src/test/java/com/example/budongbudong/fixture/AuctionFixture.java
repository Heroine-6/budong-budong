package com.example.budongbudong.fixture;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.Property;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;

import java.time.LocalDateTime;

public class AuctionFixture {

    public static Auction openEndedAuction(
            Property property,
            LocalDateTime startOfToday
    ) {
        return Auction.testBuilder()
                .property(property)
                .startPrice(1000L)
                .status(AuctionStatus.OPEN)
                .startedAt(startOfToday.minusDays(2))
                .endedAt(startOfToday.minusSeconds(1))
                .build();
    }

    public static Auction openOngoingAuction(
            Property property,
            LocalDateTime startOfToday
    ) {
        return Auction.testBuilder()
                .property(property)
                .startPrice(1000L)
                .status(AuctionStatus.OPEN)
                .startedAt(startOfToday.minusDays(1))
                .endedAt(startOfToday.plusDays(1))
                .build();
    }

}
