package com.example.budongbudong.domain.payment.dto.response;

import com.example.budongbudong.domain.payment.enums.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 상세 조회 응답 DTO
 */
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

