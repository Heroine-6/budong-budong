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

    @Column(name = "current_price")
    private BigDecimal currentPrice;

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
        auction.currentPrice = startPrice;
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
        BigDecimal rate = BigDecimal.valueOf(decreaseRate).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        return startPrice.multiply(rate);
    }

    public void updateStatus(AuctionStatus auctionStatus) {
        this.status = auctionStatus;
    }

    /**
     * 현재가 계산
     * 1. 회차: dropCount = (현재 시간 - 시작 시간) / 가격 하락 간격(30분)
     * 2. currentPrice = 시작가 - (하락금액 * dropCount)
     */
    public void recalculateCurrentPrice(long minutesElapsed) {
        if (this.status != AuctionStatus.OPEN) return;

        long dropCount = minutesElapsed / 30;
        if (dropCount <= 0) return;

        BigDecimal totalDiscount = this.decreasePrice.multiply(BigDecimal.valueOf(dropCount));
        BigDecimal targetPrice = this.startPrice.subtract(totalDiscount);

        if (targetPrice.compareTo(this.endPrice) < 0) {
            this.currentPrice = this.endPrice;

            // 하한가 도달 유찰
            this.status = AuctionStatus.FAILED;

        } else if (targetPrice.compareTo(this.currentPrice) < 0) {
            this.currentPrice = targetPrice;
        }
    }
}
