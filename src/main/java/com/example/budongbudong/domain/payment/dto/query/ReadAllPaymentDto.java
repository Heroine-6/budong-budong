package com.example.budongbudong.domain.payment.dto.query;

import com.example.budongbudong.domain.payment.enums.*;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** QueryDSL 조회 전용 DTO */
@Getter
public class ReadAllPaymentDto {
    private final Long paymentId;
    private final PaymentType type;
    private final String orderName;
    private final BigDecimal amount;
    private final PaymentStatus status;
    private final LocalDateTime approvedAt;

    @QueryProjection
    public ReadAllPaymentDto(
            Long paymentId,
            PaymentType type,
            String orderName,
            BigDecimal amount,
            PaymentStatus status,
            LocalDateTime approvedAt
    ) {
        this.paymentId = paymentId;
        this.type = type;
        this.orderName = orderName;
        this.amount = amount;
        this.status = status;
        this.approvedAt = approvedAt;
    }
}
