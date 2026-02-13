package com.example.budongbudong.domain.payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MyPaymentListResponse(
        Long auctionId,
        String auctionName,
        BigDecimal finalPrice,
        BigDecimal payableAmount,
        LocalDateTime dueAt,
        boolean expired
) {}

