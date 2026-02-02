package com.example.budongbudong.domain.payment.toss.enums;

import lombok.Getter;

/**
 * 결제 실패 사유
 * - retryable: 자동 재확인 가능 여부
 * - httpStatus: 클라이언트 응답 상태 코드
 * - message: 기본 사용자 안내 메시지
 */
@Getter
public enum PaymentFailureReason {

    AMOUNT_MISMATCH(false,400,"결제 금액이 일치하지 않습니다."),
    CARD_DECLINED(false,402,"카드 결제가 거절 되었습니다."),
    INVALID_PAYMENT_INFO(false,400,"결제 정보가 유효하지 않습니다"),

    PG_NETWORK_ERROR(true, 202, "PG 네트워크 오류로 결제 확인이 지연되고 있습니다."),
    PG_TIMEOUT(true,202,"결제 승인 응답이 지연되고 있습니다."),
    SERVER_CONFIRM_ERROR(true, 202, "서버에서 결제 결과를 확인 중입니다."),
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
