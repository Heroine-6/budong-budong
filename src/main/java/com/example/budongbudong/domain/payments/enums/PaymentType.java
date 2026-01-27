package com.example.budongbudong.domain.payments.enums;

public enum PaymentType {
    DEPOSIT("보증금"),
    DOWN_PAYMENT("계약금"),
    BALANCE("잔금");

    private final String message;

    PaymentType(String message) {
        this.message = message;
    }
}
