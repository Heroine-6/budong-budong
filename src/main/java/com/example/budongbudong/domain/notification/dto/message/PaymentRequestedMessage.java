package com.example.budongbudong.domain.notification.dto.message;

import com.example.budongbudong.domain.payment.enums.PaymentType;
import com.example.budongbudong.domain.payment.event.PaymentRequestedEvent;

import java.time.LocalDate;

public record PaymentRequestedMessage(
        Long auctionId,
        Long userId,
        PaymentType type,
        LocalDate baseDate
) {
    public static PaymentRequestedMessage from(PaymentRequestedEvent event) {
        return new PaymentRequestedMessage(
                event.auctionId(),
                event.userId(),
                event.type(),
                event.baseDate()
        );
    }
}