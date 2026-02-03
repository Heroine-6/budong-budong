package com.example.budongbudong.domain.payment.dto.response;

import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.payment.dto.query.ReadPaymentDetailDto;
import com.example.budongbudong.domain.payment.enums.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@RequiredArgsConstructor
public class ReadPaymentResponse {

    private final Long paymentId;
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

    public static ReadPaymentResponse from(ReadPaymentDetailDto dto) {
        return new ReadPaymentResponse(
                dto.getPaymentId(),
                dto.getStatus(),
                dto.getType(),
                dto.getAmount(),
                dto.getOrderName(),
                dto.getPaymentMethodType(),
                dto.getMethodDetail(),
                dto.getApprovedAt(),
                dto.getAuctionId(),
                dto.getStartPrice(),
                dto.getAuctionStatus(),
                dto.getAuctionStartedAt(),
                dto.getAuctionEndedAt()
        );
    }
}
