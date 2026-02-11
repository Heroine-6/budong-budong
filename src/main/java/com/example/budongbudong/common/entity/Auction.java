package com.example.budongbudong.common.entity;

import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.auction.enums.AuctionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private AuctionType type;

    @Column(name = "start_price", nullable = false)
    private BigDecimal startPrice;

    @Column(name = "end_price")
    private BigDecimal endPrice;

    @Column(length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private AuctionStatus status;

    //통화는 원 단위 정수로 처리하고 소수점은 사용하지 않음
    @Column(name = "min_bid_increment")
    private BigDecimal minBidIncrement;

    @Column(name = "decrease_price")
    private BigDecimal decreasePrice;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Builder(builderMethodName = "testBuilder")
    public Auction(Long id, Property property, BigDecimal startPrice, AuctionStatus status, LocalDateTime startedAt, LocalDateTime endedAt) {
        this.id = id;
        this.property = property;
        this.startPrice = startPrice;
        this.status = status;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public static Auction createEnglish(
            Property property,
            BigDecimal startPrice,
            LocalDateTime startedAt,
            LocalDateTime endedAt
    ) {
        Auction auction = new Auction();
        auction.property = property;
        auction.type = AuctionType.ENGLISH;
        auction.startPrice = startPrice;
        auction.minBidIncrement = calculateMinBidIncrement(startPrice);
        auction.status = AuctionStatus.SCHEDULED;
        auction.startedAt = startedAt;
        auction.endedAt = endedAt;
        return auction;
    }

    public static Auction createDutch(
            Property property,
            BigDecimal startPrice,
            BigDecimal endPrice,
            int decreaseRate,
            LocalDateTime startedAt
    ) {
        Auction auction = new Auction();
        auction.property = property;
        auction.type = AuctionType.DUTCH;
        auction.startPrice = startPrice;
        auction.endPrice = endPrice;
        auction.decreasePrice = calculateDecreasePrice(startPrice, decreaseRate);
        auction.status = AuctionStatus.SCHEDULED;
        auction.startedAt = startedAt;
        auction.endedAt = startedAt;
        return auction;
    }

    private static BigDecimal calculateMinBidIncrement(BigDecimal startPrice) {
        // 시작가의 10%를 올림한 값을 최소 입찰 단위로 사용.
        return startPrice.divide(BigDecimal.TEN, RoundingMode.HALF_EVEN);
    }

    private static BigDecimal calculateDecreasePrice(BigDecimal startPrice, int decreaseRate) {
        // 시작가 기준 감가율(%)에 해당하는 감가 금액
        return startPrice.multiply(BigDecimal.valueOf(decreaseRate / 100.0));
    }

    public void updateStatus(AuctionStatus auctionStatus) {
        this.status = auctionStatus;
    }
}
