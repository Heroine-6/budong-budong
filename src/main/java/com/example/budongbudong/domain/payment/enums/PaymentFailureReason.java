package com.example.budongbudong.domain.payment.enums;

import lombok.Getter;

/**
 * 결제 실패 사유
 * - retryable: 자동 재확인 가능 여부
 * - httpStatus: 클라이언트 응답 상태 코드
 * - message: 기본 사용자 안내 메시지
 */
@Getter
public enum PaymentFailureReason {

    //우리 시스템
    AMOUNT_MISMATCH(false,400,"결제 금액이 일치하지 않습니다."),
    INVALID_STATE(false,409,"이미 처리된 결제 입니다."),

    //사용자, 카드사
    CARD_DECLINED(false,402,"카드 결제가 거절 되었습니다."),
    INVALID_PAYMENT_INFO(false,400,"결제 정보가 유효하지 않습니다"),

    //PG(Toss)
    PG_NETWORK_ERROR(true, 202, "결제 확인중입니다."),
    PG_TIMEOUT(true,202,"결제 확인중입니다."),
    PG_TEMPORARY_ERROR(true, 202,"결제 확인중입니다."),
    UNKNOWN(true,202,"결제 확인 중입니다.")
    ;
    private final boolean retryable;
    private final int code;
    private final String message;

    PaymentFailureReason(boolean retryable, int code, String message) {
        this.retryable = retryable;
        this.code = code;
        this.message = message;
    }
}
