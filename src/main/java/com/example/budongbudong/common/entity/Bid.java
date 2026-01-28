package com.example.budongbudong.common.entity;

import com.example.budongbudong.domain.bid.enums.BidStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@Table(name = "bids")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bid extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private BidStatus status = BidStatus.PLACED;

    @Column(name = "is_highest", nullable = false)
    private boolean isHighest = false;

    public Bid(User user, Auction auction, BigDecimal price) {
        this.user = user;
        this.auction = auction;
        this.price = price;
        this.status = BidStatus.WINNING;
        this.isHighest = false;
    }

    public void markHighest() {
        this.isHighest = true;
    }

    public void unmarkHighest() {
        this.isHighest = false;
    }

    public void changeStatus(BidStatus status) {
        this.status = status;
    }
}
