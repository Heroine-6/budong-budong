package com.example.budongbudong.common.entity;

import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private AuctionStatus status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at", nullable = false)
    private LocalDateTime endedAt;

    @Builder(builderMethodName = "testBuilder")
    public Auction(Long id, Property property, Long startPrice, AuctionStatus status,LocalDateTime startedAt, LocalDateTime endedAt) {
        this.id = id;
        this.property = property;
        this.startPrice = startPrice;
        this.status = status;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public static Auction create(
            Property property,
            Long startPrice,
            LocalDateTime startedAt,
            LocalDateTime endedAt
    ) {
        Auction auction = new Auction();
        auction.property = property;
        auction.startPrice = startPrice;
        auction.status = AuctionStatus.SCHEDULED;
        auction.startedAt = startedAt;
        auction.endedAt = endedAt;
        return auction;
    }

    public void updateStatus(AuctionStatus auctionStatus) {
        this.status = auctionStatus;
    }
}
