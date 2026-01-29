package com.example.budongbudong.common.entity;

import com.example.budongbudong.domain.payment.enums.PaymentStatus;
import com.example.budongbudong.domain.payment.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.READY;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentType type;

    @Column(name = "order_name", nullable = false)
    private String orderName;

    @Column(name = "payment_key")
    private String paymentKey;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Builder
    public Payment(User user, Auction auction, PaymentType type, String orderName, BigDecimal amount, String orderId) {
        this.user = user;
        this.auction = auction;
        this.status = PaymentStatus.READY;
        this.type = type;
        this.orderName = orderName;
        this.amount = amount;
        this.orderId = orderId;
    }

    public void makeSuccess(String paymentKey, LocalDateTime approvedAt) {
        this.paymentKey = paymentKey;
        this.approvedAt = approvedAt;
        this.status = PaymentStatus.SUCCESS;
    }

    public void makeFail() {
        this.status = PaymentStatus.FAIL;
    }
}
