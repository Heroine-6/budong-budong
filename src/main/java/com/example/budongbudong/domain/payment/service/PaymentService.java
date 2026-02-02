package com.example.budongbudong.domain.payment.service;

import com.example.budongbudong.common.entity.*;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.common.response.CustomSliceResponse;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.payment.MQ.PaymentVerifyPublisher;
import com.example.budongbudong.domain.payment.dto.ReadAllPaymentDto;
import com.example.budongbudong.domain.payment.dto.request.PaymentConfirmRequest;
import com.example.budongbudong.domain.payment.dto.response.PaymentTossReadyResponse;
import com.example.budongbudong.domain.payment.dto.response.ReadAllPaymentResponse;
import com.example.budongbudong.domain.payment.enums.*;
import com.example.budongbudong.domain.payment.repository.PaymentRepository;
import com.example.budongbudong.domain.payment.toss.client.TossPaymentClient;
import com.example.budongbudong.domain.payment.toss.enums.PaymentFailureReason;
import com.example.budongbudong.domain.payment.toss.exception.TossClientException;
import com.example.budongbudong.domain.payment.toss.exception.TossNetworkException;
import com.example.budongbudong.domain.payment.utils.PaymentAmountCalculator;
import com.example.budongbudong.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
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
    public PaymentTossReadyResponse requestPayment(Long userId, Long auctionId, PaymentType type) {

        Auction auction = auctionRepository.getAuctionWithPropertyOrTrow(auctionId);
        User user = userRepository.getByIdOrThrow(userId);

        paymentRepository.findByAuctionAndTypeAndStatus(auction,type, PaymentStatus.SUCCESS)
                .ifPresent(p -> {
                    throw new CustomException(ErrorCode.ALREADY_PAID);
                });

        Optional<Payment> inProgressPayment = paymentRepository.findByAuctionAndTypeAndStatus(auction,type,PaymentStatus.IN_PROGRESS);
        if (inProgressPayment.isPresent()) {
            return PaymentTossReadyResponse.from(inProgressPayment.get());
        }

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

        return PaymentTossReadyResponse.from(payment);
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
        if (payment.isFinalized()) return;

        // 결제 확인 중인 상태는 confirm 호출 차단
        if (payment.getStatus().equals(PaymentStatus.VERIFYING)) return ;

        //금액 검증
        if (payment.getAmount().compareTo(request.amount()) != 0){
            payment.makeFail(PaymentFailureReason.AMOUNT_MISMATCH);
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        //toss 승인 호출
        try{
            tossPaymentClient.confirm(request.paymentKey(), request.orderId(), request.amount());
        } catch(TossClientException e) {
            payment.makeFail(PaymentFailureReason.INVALID_PAYMENT_INFO);
            return;
        } catch(TossNetworkException e) {
            makeVerifyingAndPublish(payment,request,PaymentFailureReason.PG_NETWORK_ERROR);
            return;
        }

        // 승인 확정 -> SUCCESS 상태 전이
        try {
            finalizePayment(payment, request);
            payment.makeSuccess(request.paymentKey(), LocalDateTime.now());
        } catch(Exception e) {
            makeVerifyingAndPublish(payment,request, PaymentFailureReason.SERVER_CONFIRM_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public CustomSliceResponse<ReadAllPaymentResponse> getAllPaymentList(Long userId, Pageable pageable) {

        Slice<ReadAllPaymentDto> payments = paymentRepository.findAllByUserId(userId, pageable);
        Slice<ReadAllPaymentResponse> response = payments.map(ReadAllPaymentResponse::from);
        return CustomSliceResponse.from(response.getContent(), pageable.getPageSize(), pageable.getPageNumber(), response.hasNext());
    }

    private void makeVerifyingAndPublish(
            Payment payment,
            PaymentConfirmRequest request,
            PaymentFailureReason reason
    ) {

        payment.makeVerifying(reason, request.paymentKey());
        //일정 시간 후 재확인을 위한 MQ 트리거
        verifyPublisher.publish(payment.getId(), 30000L);
    }

    /**
     * PG 승인 이후,서버가 결제 결과를 "확정"하는 단계
     * - 지금은 확정의 경계 역할만 한다.
     */
    private void finalizePayment(Payment payment, PaymentConfirmRequest request) {

        paymentRepository.save(payment);
    }
}
