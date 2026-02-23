package com.example.budongbudong.domain.payment.dto.request;

import com.example.budongbudong.domain.payment.enums.PaymentType;

public record PaymentRequest(PaymentType type) {
}
