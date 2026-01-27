package com.example.budongbudong.domain.payments.enums;

public enum PaymentStatus {
    PENDING("결제 대기"),
    PAID("결제 완료"),
    FAIL("결제 실패"),
    CANCEL("결제 취소"),
    REFUND("환불");

    private final String message;

    PaymentStatus(String message) {
        this.message = message;
    }
}
