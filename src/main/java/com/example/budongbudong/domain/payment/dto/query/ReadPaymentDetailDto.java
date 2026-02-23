package com.example.budongbudong.domain.payment.dto.query;

import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.payment.enums.PaymentMethodType;
import com.example.budongbudong.domain.payment.enums.PaymentStatus;
import com.example.budongbudong.domain.payment.enums.PaymentType;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** QueryDSL 조회 전용 DTO */
@Getter
public class ReadPaymentDetailDto {

    private final Long paymentId;
    private final Long userId;
    private final PaymentStatus status;
    private final PaymentType type;
    private final BigDecimal amount;
    private final String orderName;
    private final PaymentMethodType paymentMethodType;
    private final String methodDetail;
    private final LocalDateTime approvedAt;
    private final Long auctionId;
    private final BigDecimal startPrice;
    private final AuctionStatus auctionStatus;
    private final LocalDateTime auctionStartedAt;
    private final LocalDateTime auctionEndedAt;

    @QueryProjection
    public ReadPaymentDetailDto(
            Long paymentId,
            Long userId,
            PaymentStatus status,
            PaymentType type,
            BigDecimal amount,
            String orderName,
            PaymentMethodType paymentMethodType,
            String methodDetail,
            LocalDateTime approvedAt,
            Long auctionId,
            BigDecimal startPrice,
            AuctionStatus auctionStatus,
            LocalDateTime auctionStartedAt,
            LocalDateTime auctionEndedAt
    ) {
        this.paymentId = paymentId;
        this.userId = userId;
        this.status = status;
        this.type = type;
        this.amount = amount;
        this.orderName = orderName;
        this.paymentMethodType = paymentMethodType;
        this.methodDetail = methodDetail;
        this.approvedAt = approvedAt;
        this.auctionId = auctionId;
        this.startPrice = startPrice;
        this.auctionStatus = auctionStatus;
        this.auctionStartedAt = auctionStartedAt;
        this.auctionEndedAt = auctionEndedAt;
    }
}
