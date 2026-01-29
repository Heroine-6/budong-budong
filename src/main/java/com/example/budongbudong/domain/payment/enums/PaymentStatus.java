package com.example.budongbudong.domain.payment.enums;

public enum PaymentStatus {
    READY("결제 전"),
    PROCESSING("결제 진행중"),
    SUCCESS("결제 성공"),
    FAIL("결제 실패");

    private final String message;

    PaymentStatus(String message) {
        this.message = message;
    }
}
