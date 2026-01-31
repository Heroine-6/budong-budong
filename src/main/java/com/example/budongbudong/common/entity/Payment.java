package com.example.budongbudong.common.entity;

import com.example.budongbudong.domain.payment.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Duration;
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

    @Column(name = "failure_reason")
    @Enumerated(EnumType.STRING)
    private PaymentFailureReason failureReason;

    @Column(name="verifying_started_at")
    private LocalDateTime verifyingStartedAt;

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
        this.failureReason = null;
    }

    /**
     * 승인 결과 미확정 상태 처리
     * - PG 장애 또는 네트워크 오류 시 사용
     * - 배치 재확인 대상
     */
    public void makeFail(PaymentFailureReason failureReason) {
        this.status = PaymentStatus.FAIL;
        this.failureReason = failureReason;
    }

    public void makeVerifying(PaymentFailureReason failureReason, String paymentKey) {
        this.status = PaymentStatus.VERIFYING;
        this.failureReason = failureReason;
        this.paymentKey = paymentKey;
        if(this.verifyingStartedAt == null) {
            this.verifyingStartedAt = LocalDateTime.now();
        }
    }

    /**
     * 결제가 최종 확정 상태인지 여부
     * MQ 중복 및 재전송에 대한 멱등성 보장
     */
    public boolean isFinalized() {
        return status == PaymentStatus.SUCCESS || status == PaymentStatus.FAIL;
    }

    public boolean isVerifiedTimeout(Duration limit) {
        return verifyingStartedAt != null && verifyingStartedAt.isBefore(LocalDateTime.now().minus(limit));
    }

}
