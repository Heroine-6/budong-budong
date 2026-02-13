package com.example.budongbudong.domain.payment.dto.response;

import com.example.budongbudong.domain.payment.enums.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentInfoResponse(
        Long auctionId,
        String auctionName,

        PaymentType type,

        BigDecimal finalPrice,
        BigDecimal payableAmount,

        BigDecimal alreadyPaidAmount, // 잔금용
        Integer rate, // 계약금 용

        LocalDateTime bidAt,
        LocalDateTime wonAt,
        LocalDateTime dueAt
) {}

