package com.example.budongbudong.domain.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentConfirmRequest(

        @NotBlank(message = "paymentKey는 필수입니다.")
        String paymentKey,
        @NotBlank(message = "orderId는 필수입니다.")
        String orderId,
        @NotBlank(message = "amount는 필수입니다.")
        @Positive
        BigDecimal amount
) {
}
