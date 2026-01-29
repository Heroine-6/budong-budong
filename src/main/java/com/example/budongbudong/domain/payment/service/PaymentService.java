package com.example.budongbudong.domain.payment.service;

import com.example.budongbudong.common.entity.*;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.payment.dto.request.PaymentConfirmRequest;
import com.example.budongbudong.domain.payment.dto.response.PaymentRequestResponse;
import com.example.budongbudong.domain.payment.enums.PaymentType;
import com.example.budongbudong.domain.payment.repository.PaymentRepository;
import com.example.budongbudong.domain.payment.toss.client.TossPaymentClient;
import com.example.budongbudong.domain.payment.utils.PaymentAmountCalculator;
import com.example.budongbudong.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final PaymentAmountCalculator calculator;
    private final TossPaymentClient tossPaymentClient;

    @Transactional
    public PaymentRequestResponse requestPayment(Long userId, Long auctionId, PaymentType type) {

        Auction auction = auctionRepository.getAuctionWithPropertyOrTrow(auctionId);
        User user = userRepository.getByIdOrThrow(userId);

        BigDecimal amount = calculator.calculate(auction, type);
        String orderName = auction.getProperty().getName(); //매물의 name field
        String orderId = UUID.randomUUID().toString();

        Payment payment = Payment.builder()
                .user(user)
                .auction(auction)
                .type(type)
                .orderName(orderName)
                .amount(amount)
                .orderId(orderId)
                .build();

        paymentRepository.save(payment);

        return PaymentRequestResponse.from(payment);
    }

    @Transactional
    public void confirmPayment(PaymentConfirmRequest request) {

        Payment payment = paymentRepository.getByOrderIdOrThrow(request.orderId());

        //금액 검증
        if (payment.getAmount().compareTo(request.amount()) != 0){
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        //toss 승인 호출
        tossPaymentClient.confirm(request.paymentKey(), request.orderId(), request.amount());

        payment.makeSuccess(request.paymentKey(), LocalDateTime.now());
    }
}
