package com.example.budongbudong.domain.payment.toss.enums;

import lombok.Getter;

@Getter
public enum TossPaymentStatus {
    SUCCESS("성공"),
    FAIL("실패"),
    UNKNOWN("확정 할 수 없음")
    ;
    private final String message;
    TossPaymentStatus(String message) {
        this.message = message;
    }

    public boolean isFinalized() {
        return this == SUCCESS || this == FAIL;
    }
}
