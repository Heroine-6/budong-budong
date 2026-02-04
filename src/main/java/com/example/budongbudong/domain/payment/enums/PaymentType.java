package com.example.budongbudong.domain.payment.enums;

public enum PaymentType {
    DEPOSIT(true,true,"보증금"),
    DOWN_PAYMENT(true,false,"계약금"),
    BALANCE(false,false,"잔금");

    private final boolean refundable;
    private final boolean autoRetry;
    private final String message;

    PaymentType(Boolean refundable,Boolean autoRetry ,String message) {
        this.refundable = refundable;
        this.autoRetry = autoRetry;
        this.message = message;
    }
}
