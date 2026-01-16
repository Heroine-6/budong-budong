package com.example.budongbudong.domain.auctionwinner.entity;


import com.example.budongbudong.common.entity.BaseEntity;
import com.example.budongbudong.domain.auction.entity.Auction;
import com.example.budongbudong.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private Long price;

    public static AuctionWinner create(Auction auction, User user, Long price){
        AuctionWinner winner = new AuctionWinner();
        winner.auction = auction;
        winner.user = user;
        winner.price = price;
        return  winner;
    }
}
