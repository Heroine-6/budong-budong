package com.example.budongbudong.domain.payment.dto.response;

import com.example.budongbudong.common.entity.Payment;
import com.example.budongbudong.domain.payment.enums.PaymentDisplayStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReadAllPaymentResponse {
    private final Long paymentId;
    private final String orderName;
    private final BigDecimal amount;
    private final PaymentDisplayStatus status;
    private final LocalDateTime approvedAt;

    public static ReadAllPaymentResponse from(Payment payment){
        return new ReadAllPaymentResponse(
                payment.getId(),
                payment.getOrderName(),
                payment.getAmount(),
                PaymentDisplayStatus.from(payment.getStatus()),
                payment.getApprovedAt()
        );
    }
}
