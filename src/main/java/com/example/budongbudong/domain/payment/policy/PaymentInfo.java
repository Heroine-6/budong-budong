package com.example.budongbudong.domain.payment.policy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 정책 계산 결과를 담는 화면 전용 도메인 객체
 */
public record PaymentInfo(
        BigDecimal payableAmount,
        BigDecimal alreadyPaidAmount,
        Integer rate,
        LocalDateTime dueAt
) {}

