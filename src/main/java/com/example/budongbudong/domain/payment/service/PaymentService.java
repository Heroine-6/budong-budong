package com.example.budongbudong.domain.payment.service;

import com.example.budongbudong.common.entity.*;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.payment.MQ.PaymentVerifyPublisher;
import com.example.budongbudong.domain.payment.dto.request.PaymentConfirmRequest;
import com.example.budongbudong.domain.payment.dto.response.PaymentRequestResponse;
import com.example.budongbudong.domain.payment.enums.PaymentFailureReason;
import com.example.budongbudong.domain.payment.enums.PaymentType;
import com.example.budongbudong.domain.payment.repository.PaymentRepository;
import com.example.budongbudong.domain.payment.toss.client.TossPaymentClient;
import com.example.budongbudong.domain.payment.toss.exception.TossClientException;
import com.example.budongbudong.domain.payment.toss.exception.TossNetworkException;
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
    private final PaymentVerifyPublisher verifyPublisher;

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

    /**
     * 결제 승인 처리
     * - 성공 시 즉시 SUCCESS
     * - 네트워크 장애 시 VERIFYING 전이 후 MQ 트리거 발행
     */
    @Transactional
    public void confirmPayment(PaymentConfirmRequest request) {

        Payment payment = paymentRepository.getByOrderIdOrThrow(request.orderId());

        //이미 확정된 결제 중복 처리 방지
        if(payment.isFinalized()) {
            return;
        }
        //금액 검증
        if (payment.getAmount().compareTo(request.amount()) != 0){
            payment.makeFail(PaymentFailureReason.AMOUNT_MISMATCH);
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        //toss 승인 호출
        try{
            tossPaymentClient.confirm(request.paymentKey(), request.orderId(), request.amount());
            payment.makeSuccess(request.paymentKey(), LocalDateTime.now());
        } catch(TossClientException e) {
            payment.makeFail(PaymentFailureReason.INVALID_PAYMENT_INFO);
        } catch(TossNetworkException e) {
            payment.makeVerifying(PaymentFailureReason.PG_NETWORK_ERROR, request.paymentKey());
            //일정 시간 후 재확인을 위한 MQ 트리거
            verifyPublisher.publish(payment.getId(), 60000L);
        }

    }
}
