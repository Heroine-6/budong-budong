package com.example.budongbudong.domain.payment.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PaymentConfirmRequest(

        @NotBlank(message = "paymentKey는 필수입니다.")
        String paymentKey,
        @NotBlank(message = "orderId는 필수입니다.")
        String orderId,
        @NotNull(message = "amount는 필수입니다.")
        @Positive
        BigDecimal amount
) {
}
