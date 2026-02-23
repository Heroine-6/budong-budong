package com.example.budongbudong.domain.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentDisplayStatus {
    PAID("결제 완료"),
    FAILED("결제 실패"),
    IN_PROGRESS("결제 진행중"),
    REFUND_IN_PROGRESS("환불 처리중"),
    REFUNDED("환불 완료");

    private final String message;

    public static PaymentDisplayStatus from(PaymentStatus status) {
        return switch(status) {
            case SUCCESS -> PAID;
            case VERIFYING -> IN_PROGRESS;
            case FAIL -> FAILED;
            case REFUND_REQUESTED -> REFUND_IN_PROGRESS;
            case REFUNDED -> PaymentDisplayStatus.REFUNDED;
            default -> IN_PROGRESS;
        };
    }
}
