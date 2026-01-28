package com.example.budongbudong.common.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "auction_winners")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuctionWinner extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private BigDecimal price;

    public static AuctionWinner create(Auction auction, User user, BigDecimal price) {
        AuctionWinner winner = new AuctionWinner();
        winner.auction = auction;
        winner.user = user;
        winner.price = price;
        return winner;
    }
}
