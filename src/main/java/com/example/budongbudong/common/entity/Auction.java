package com.example.budongbudong.common.entity;

import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "auctions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Auction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "start_price", nullable = false)
    private Long startPrice;

    @Column(name = "min_bid_increment", nullable = false)
    private Long minBidIncrement;

    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private AuctionStatus status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at", nullable = false)
    private LocalDateTime endedAt;

    public static Auction create(
            Property property,
            Long startPrice,
            LocalDateTime startedAt,
            LocalDateTime endedAt
    ) {
        Auction auction = new Auction();
        auction.property = property;
        auction.startPrice = startPrice;
        auction.minBidIncrement = calculateMinBidIncrement(startPrice);
        auction.status = AuctionStatus.SCHEDULED;
        auction.startedAt = startedAt;
        auction.endedAt = endedAt;
        return auction;
    }

    private static Long calculateMinBidIncrement(Long startPrice) {
        return (startPrice + 9) / 10;
    }

    public void updateStatus(AuctionStatus auctionStatus) {
        this.status = auctionStatus;
    }
}
