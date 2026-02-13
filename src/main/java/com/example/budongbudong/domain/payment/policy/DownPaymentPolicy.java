package com.example.budongbudong.domain.payment.policy;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.AuctionWinner;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.payment.enums.PaymentStatus;
import com.example.budongbudong.domain.payment.enums.PaymentType;
import com.example.budongbudong.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DownPaymentPolicy implements PaymentPolicy {

    private final PaymentRepository paymentRepository;

    @Override
    public PaymentType supports() {
        return PaymentType.DOWN_PAYMENT;
    }

    @Override
    public PaymentInfo assembleInfo(Auction auction, AuctionWinner winner, Long userId) {

        if (!winner.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ONLY_CAN_PAY_BIDDER);
        }

        if (auction.getStatus() != AuctionStatus.CLOSED) {
            throw new CustomException(ErrorCode.ONLY_CAN_DO_CLOSED_AUCTION);
        }

        boolean alreadyPaid =
                paymentRepository.existsByAuctionIdAndTypeAndStatus(
                        auction.getId(),
                        PaymentType.DOWN_PAYMENT,
                        PaymentStatus.SUCCESS
                );

        if (alreadyPaid) {
            throw new CustomException(ErrorCode.ALREADY_PAID);
        }

        BigDecimal amount = winner.getPrice()
                .multiply(BigDecimal.valueOf(0.1))
                .setScale(0, RoundingMode.DOWN);

        LocalDateTime dueAt = winner.getCreatedAt().plusHours(24);

        if (LocalDateTime.now().isAfter(dueAt)) {
            throw new CustomException(ErrorCode.PAYMENT_EXPIRED);
        }

        return new PaymentInfo(
                amount,
                null,        // 계약금은 이미 낸 금액 의미 없음
                10,          // rate
                dueAt
        );
    }
}

