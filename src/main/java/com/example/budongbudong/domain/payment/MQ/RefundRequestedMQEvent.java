package com.example.budongbudong.domain.payment.MQ;

import lombok.*;

import java.util.Objects;

/**
 * 환불 요청을 MQ로 전달하기 위한 메시지 객체
 * - 환불 대상 결제 ID를 포함한다
 */
@Getter
public class RefundRequestedMQEvent {

    private final Long paymentId;

    public RefundRequestedMQEvent(Long paymentId) {
        this.paymentId = Objects.requireNonNull(paymentId, "paymentId must not be null");
    }
}

