package com.example.budongbudong.domain.payment.log.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LogType {
    STATUS_CHANGE("상태 변경"),

    PAYMENT_SUCCESS("결제 성공"),

    REFUND_REQUESTED("환불 요청"),
    REFUND_RETRY("환불 재시도"),
    REFUND_SUCCESS("환불 성공"),
    REFUND_FAILED("환불 실패"),

    TOSS_CLIENT_ERROR("토스 장애"),
    TOSS_NETWORK_ERROR("토스 네트워크 장애"),

    MANUAL_REFUND_REQUESTED("수동 환불 요청"),
    ;

    private final String message;
}
