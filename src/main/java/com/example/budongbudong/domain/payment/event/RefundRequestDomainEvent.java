package com.example.budongbudong.domain.payment.event;
/**
 * 결제 환불을 요청했음을 나타내는 도메인 이벤트
 * - 환불 대상 결제 ID만을 포함한다
 */
public record RefundRequestDomainEvent(Long paymentId) {
}
