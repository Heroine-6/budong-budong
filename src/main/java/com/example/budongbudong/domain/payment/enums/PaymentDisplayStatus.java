package com.example.budongbudong.domain.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentDisplayStatus {
    PAID("결제 완료"),
    FAILED("결제 실패"),
    IN_PROGRESS("결제 진행중");
    //TODO 결제 환불
    private final String message;

    public static PaymentDisplayStatus from(PaymentStatus status) {
        return switch(status) {
            case SUCCESS -> PAID;
            case VERIFYING ->  IN_PROGRESS; //사용자 UX 관점
            case FAIL -> FAILED;
            default -> IN_PROGRESS;
        };
    }
}
