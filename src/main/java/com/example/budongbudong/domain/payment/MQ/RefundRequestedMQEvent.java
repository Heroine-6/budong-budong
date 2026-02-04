package com.example.budongbudong.domain.payment.MQ;

import lombok.*;

/**
 * 환불 요청을 MQ로 전달하기 위한 메시지 객체
 * - 환불 대상 결제 ID를 포함한다
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequestedMQEvent {
    private Long paymentId;
}
