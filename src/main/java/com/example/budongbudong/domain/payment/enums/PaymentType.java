package com.example.budongbudong.domain.payment.enums;

import lombok.Getter;

@Getter
public enum PaymentType {
    DEPOSIT("보증금", 0),
    DOWN_PAYMENT("계약금", 1),
    BALANCE("잔금", 14);

    private final String message;
    private final int dueDays;

    PaymentType(String message, int dueDays) {
        this.message = message;
        this.dueDays = dueDays;
    }
}
