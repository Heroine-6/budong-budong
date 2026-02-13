package com.example.budongbudong.domain.payment.dto.query;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** QueryDSL 조회 전용 DTO */
@Getter
public class RequiredPaymentDto {

    private final Long auctionId;
    private final String auctionName;
    private final BigDecimal finalPrice;
    private final BigDecimal paidAmount;
    private final LocalDateTime winnerCreatedAt;
    private final LocalDateTime downPaymentApprovedAt;

    @QueryProjection
    public RequiredPaymentDto(
            Long auctionId,
            String auctionName,
            BigDecimal finalPrice,
            BigDecimal paidAmount,
            LocalDateTime winnerCreatedAt,
            LocalDateTime downPaymentApprovedAt
    ) {
        this.auctionId = auctionId;
        this.auctionName = auctionName;
        this.finalPrice = finalPrice;
        this.paidAmount = paidAmount;
        this.winnerCreatedAt = winnerCreatedAt;
        this.downPaymentApprovedAt = downPaymentApprovedAt;
    }
}
