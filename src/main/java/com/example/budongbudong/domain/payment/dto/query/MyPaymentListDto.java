package com.example.budongbudong.domain.payment.dto.query;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class MyPaymentListDto {

    private Long auctionId;
    private String auctionName;
    private BigDecimal finalPrice;
    private BigDecimal payableAmount;
    private LocalDateTime dueAt;
    private boolean expired;
}