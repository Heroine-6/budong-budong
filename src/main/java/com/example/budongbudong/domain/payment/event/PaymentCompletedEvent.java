package com.example.budongbudong.domain.payment.event;

/**
 * 결제가 완료되었음을 나타내는 도메인 이벤트 객체
 * - 알림 발송
 */
public record PaymentCompletedEvent(Long auctionId, Long userId, Long paymentId) {
}
