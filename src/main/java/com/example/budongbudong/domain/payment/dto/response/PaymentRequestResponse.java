package com.example.budongbudong.domain.payment.dto.response;

import com.example.budongbudong.common.entity.Payment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public class PaymentRequestResponse {
    private final String orderId;
    private final BigDecimal amount;
    private final String orderName;

    public static PaymentRequestResponse from(Payment payment) {
        return new PaymentRequestResponse(
                payment.getOrderId(),
                payment.getAmount(),
                payment.getOrderName()
        );
    }
}
