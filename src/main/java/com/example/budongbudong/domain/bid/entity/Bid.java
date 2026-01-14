package com.example.budongbudong.domain.bid.entity;

import com.example.budongbudong.common.entity.BaseEntity;
import com.example.budongbudong.domain.auction.entity.Auction;
import com.example.budongbudong.domain.bid.enums.BidStatus;
import com.example.budongbudong.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private Long price;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private BidStatus status;

    @Column(name = "is_highest", nullable = false)
    private boolean isHighest = true;

    public Bid(User user, Auction auction, Long price) {
        this.user = user;
        this.auction = auction;
        this.price = price;
        this.status = BidStatus.PLACED;
        this.isHighest = true;
    }

    public void markAsHighest() {
        this.isHighest = true;
    }

    public void unmarkHighest() {
        this.isHighest = false;
    }

    public void changeStatus(BidStatus status) {
        this.status = status;
    }

}
