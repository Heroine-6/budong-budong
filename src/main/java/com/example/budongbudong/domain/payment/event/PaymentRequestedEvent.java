package com.example.budongbudong.domain.payment.event;

import com.example.budongbudong.domain.payment.enums.PaymentType;

import java.time.LocalDate;

/**
 * 결제가 요청되었음을 나타내는 도메인 이벤트 객체
 * - 알림 발송
 */
public record PaymentRequestedEvent(Long auctionId, Long userId, PaymentType type, LocalDate baseDate) {
}
