package com.example.budongbudong.domain.notification.dto.message;

import com.example.budongbudong.domain.payment.event.PaymentCompletedEvent;

public record PaymentCompletedMessage(Long paymentId, Long userId) {
    public static PaymentCompletedMessage from(PaymentCompletedEvent event) {
        return new PaymentCompletedMessage(event.paymentId(), event.userId());
    }
}