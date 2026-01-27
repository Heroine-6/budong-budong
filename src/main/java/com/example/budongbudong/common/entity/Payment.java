package com.example.budongbudong.common.entity;

import com.example.budongbudong.domain.payments.enums.PaymentStatus;
import com.example.budongbudong.domain.payments.enums.PaymentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentType type;

    @Column(name = "order_name", nullable = false)
    private String orderName;

    @Column(name = "payment_key", nullable = false)
    private String paymentKey;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
}
