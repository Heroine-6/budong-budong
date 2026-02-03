package com.example.budongbudong.common.entity;

import com.example.budongbudong.domain.payment.enums.*;
import com.example.budongbudong.domain.payment.toss.enums.PaymentFailureReason;
import com.example.budongbudong.domain.payment.toss.enums.TossPaymentStatus;
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

    @Column(name = "status", nullable = false, columnDefinition = "varchar(30)")
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.READY;

    @Column(name = "type", nullable = false, columnDefinition = "varchar(30)")
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

    @Column(name = "failure_reason", columnDefinition = "varchar(50)")
    @Enumerated(EnumType.STRING)
    private PaymentFailureReason failureReason;

    @Column(name="verifying_started_at")
    private LocalDateTime verifyingStartedAt;

    @Column(name = "payment_method_type", columnDefinition = "varchar(50)")
    @Enumerated(EnumType.STRING)
    private PaymentMethodType paymentMethodType;

    @Column(name = "method_detail")
    private String methodDetail;

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

    public void makeSuccess(String paymentKey, LocalDateTime approvedAt,
                            PaymentMethodType paymentMethodType, String methodDetail) {
        this.paymentKey = paymentKey;
        this.approvedAt = approvedAt;
        this.status = PaymentStatus.SUCCESS;
        this.failureReason = null;
        if (paymentMethodType != null) {
            this.paymentMethodType = paymentMethodType;
        }
        if (methodDetail != null) {
            this.methodDetail = methodDetail;
        }
    }

    public void makeFail(PaymentFailureReason failureReason) {
        this.status = PaymentStatus.FAIL;
        this.failureReason = failureReason;
    }

    /**
     * 승인 결과 미확정 상태 처리
     * - PG 장애 또는 네트워크 오류 시 사용
     * - 배치 재확인 대상
     */
    public void makeVerifying(PaymentFailureReason failureReason, String paymentKey) {
        this.status = PaymentStatus.VERIFYING;
        this.failureReason = failureReason;
        this.paymentKey = paymentKey;
        if(this.verifyingStartedAt == null) {
            this.verifyingStartedAt = LocalDateTime.now();
        }
    }

    public void makeInProgress(String paymentKey) {
        if(this.status != PaymentStatus.READY) return;
        this.status = PaymentStatus.IN_PROGRESS;
        this.paymentKey = paymentKey;
    }

    public void finalizeByTossStatus(TossPaymentStatus status,
                                     PaymentMethodType paymentMethodType, String methodDetail) {
        if(this.status != PaymentStatus.VERIFYING) return;

        switch (status) {
            case SUCCESS -> makeSuccess(this.paymentKey, LocalDateTime.now(), paymentMethodType, methodDetail);
            case FAIL -> makeFail(PaymentFailureReason.UNKNOWN);
            case UNKNOWN -> {
                //그대로 VERIFYING 유지
            }
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
