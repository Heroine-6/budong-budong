package com.example.budongbudong.domain.payment.policy;

import com.example.budongbudong.common.entity.*;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.payment.enums.PaymentStatus;
import com.example.budongbudong.domain.payment.enums.PaymentType;
import com.example.budongbudong.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 잔금 결제 정책
 * 계약금 선납 여부 및 7일 기한 검증 로직 담당
 */
@Component
@RequiredArgsConstructor
public class BalancePaymentPolicy implements PaymentPolicy {

    private final PaymentRepository paymentRepository;

    @Override
    public PaymentType supports() {
        return PaymentType.BALANCE;
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
                        PaymentType.BALANCE,
                        PaymentStatus.SUCCESS
                );

        if (alreadyPaid) {
            throw new CustomException(ErrorCode.ALREADY_PAID);
        }

        BigDecimal paidAmount = paymentRepository.sumPaidAmountByAuctionId(
                        auction.getId(),
                        List.of(PaymentType.DEPOSIT, PaymentType.DOWN_PAYMENT)
                );

        BigDecimal remaining = winner.getPrice().subtract(paidAmount);

        //음수 방지
        if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException(ErrorCode.ALREADY_PAID);
        }

        Payment downPayment = paymentRepository.getSuccessDownPayment(auction.getId());

        LocalDateTime dueAt = downPayment.getApprovedAt().plusDays(7);

        if (LocalDateTime.now().isAfter(dueAt)) {
            throw new CustomException(ErrorCode.PAYMENT_EXPIRED);
        }

        return new PaymentInfo(
                remaining,
                paidAmount,
                null,
                dueAt
        );
    }
}

