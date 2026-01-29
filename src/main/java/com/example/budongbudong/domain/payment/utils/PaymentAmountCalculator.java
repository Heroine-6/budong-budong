package com.example.budongbudong.domain.payment.utils;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.AuctionWinner;
import com.example.budongbudong.domain.auctionwinner.repository.AuctionWinnerRepository;
import com.example.budongbudong.domain.payment.enums.PaymentType;
import com.example.budongbudong.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 결제 금액 계산 전용 컴포넌트
 */
@Component
@RequiredArgsConstructor
public class PaymentAmountCalculator {

    private final AuctionWinnerRepository  auctionWinnerRepository;
    private final PaymentRepository paymentRepository;

    public BigDecimal calculate(Auction auction, PaymentType type){

        AuctionWinner auctionWinner = auctionWinnerRepository.getAuctionWinnerOrThrow(auction.getId());

        return switch (type) {
            case DEPOSIT -> calculateDeposit(auction);
            case DOWN_PAYMENT -> calculateDownPayment(auctionWinner);
            case BALANCE -> calculateBalance(auction, auctionWinner);
        };
    }

    //시작가의 10%
    private BigDecimal calculateDeposit(Auction auction){
        return auction.getStartPrice().multiply(BigDecimal.valueOf(0.1)).setScale(0, RoundingMode.DOWN);
    }

    //낙찰가의 10%
    private BigDecimal calculateDownPayment(AuctionWinner auctionWinner){
        return auctionWinner.getPrice().multiply(BigDecimal.valueOf(0.1)).setScale(0, RoundingMode.DOWN);
    }

    //낙찰가 - 지불한 금액
    private BigDecimal calculateBalance(Auction auction, AuctionWinner auctionWinner){
        BigDecimal paidAmount = paymentRepository.sumPaidAmountByAuctionId(auction.getId());
        return auctionWinner.getPrice().subtract(paidAmount);
    }
}
