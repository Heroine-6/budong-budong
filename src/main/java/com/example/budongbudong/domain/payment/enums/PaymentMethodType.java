package com.example.budongbudong.domain.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethodType {
    CARD("카드"),
    EASY_PAY("간편결제");

    private final String message;

    public static PaymentMethodType from(String method) {
        for (PaymentMethodType type : values()) {
            if (type.message.equals(method)) {
                return type;
            }
        }
        throw new IllegalArgumentException("알 수 없는 결제 수단: " + method);
    }
}
