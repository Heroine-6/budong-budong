package com.example.budongbudong.domain.payment.dto.response;

import com.example.budongbudong.domain.payment.dto.ReadAllPaymentDto;
import com.example.budongbudong.domain.payment.enums.PaymentDisplayStatus;
import com.example.budongbudong.domain.payment.enums.PaymentType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ReadAllPaymentResponse {
    private final Long paymentId;
    private final PaymentType type;
    private final String orderName;
    private final BigDecimal amount;
    private final PaymentDisplayStatus status;
    private final LocalDateTime approvedAt;

    public static ReadAllPaymentResponse from(ReadAllPaymentDto payment){
        return new ReadAllPaymentResponse(
                payment.getPaymentId(),
                payment.getType(),
                payment.getOrderName(),
                payment.getAmount(),
                PaymentDisplayStatus.from(payment.getStatus()),
                payment.getApprovedAt()
        );
    }
}
