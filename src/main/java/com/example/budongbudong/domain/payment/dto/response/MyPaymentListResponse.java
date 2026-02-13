package com.example.budongbudong.domain.payment.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 계약금/잔금 결제 대상 목록 조회 응답 DTO
 */
public record MyPaymentListResponse(
        Long auctionId,
        String auctionName,
        BigDecimal finalPrice,
        BigDecimal payableAmount,
        LocalDateTime dueAt,
        boolean expired
) {}

