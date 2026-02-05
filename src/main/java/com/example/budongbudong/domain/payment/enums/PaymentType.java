package com.example.budongbudong.domain.payment.enums;

import lombok.Getter;

@Getter
public enum PaymentType {
    DEPOSIT(true,true,"보증금", 0),
    DOWN_PAYMENT(true,false,"계약금", 1),
    BALANCE(false,false,"잔금", 14);

    private final boolean refundable;
    private final boolean autoRetry;
    private final String message;
    private final int dueDays;

    PaymentType(Boolean refundable, Boolean autoRetry ,String message, int dueDays) {
        this.refundable = refundable;
        this.autoRetry = autoRetry;
        this.message = message;
        this.dueDays = dueDays;
    }
}
