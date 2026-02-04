package com.example.budongbudong.domain.payment.enums;

import lombok.Getter;

/**
 * 결제 처리 상태
 * - READY: 결제 요청 생성 완료 (승인 전)
 * - PROCESSING: 승인 요청 전송, 결과 미확정
 * - VERIFYING: 승인 결과 유실, PG 재확인 대상
 * - SUCCESS: 승인 확정
 * - FAIL: 승인 실패 확정
 */
@Getter
public enum PaymentStatus {
    READY("결제 미완료"),
    IN_PROGRESS("결제 처리 중"),
    VERIFYING("결제 확인 중"),
    SUCCESS("결제 성공"),
    FAIL("결제 실패"),

    REFUND_REQUESTED("환불 처리 중"),
    REFUNDED("환불 완료"),
    ;
    private final String message;

    PaymentStatus(String message) {
        this.message = message;
    }
}
