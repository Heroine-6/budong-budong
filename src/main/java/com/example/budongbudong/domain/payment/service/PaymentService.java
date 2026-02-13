package com.example.budongbudong.domain.payment.service;

import com.example.budongbudong.common.entity.*;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.common.response.CustomSliceResponse;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.auctionwinner.repository.AuctionWinnerRepository;
import com.example.budongbudong.domain.payment.MQ.PaymentReconfirmPublisher;
import com.example.budongbudong.domain.payment.dto.query.*;
import com.example.budongbudong.domain.payment.dto.request.PaymentConfirmRequest;
import com.example.budongbudong.domain.payment.dto.response.*;
import com.example.budongbudong.domain.payment.enums.PaymentMethodType;
import com.example.budongbudong.domain.payment.enums.PaymentStatus;
import com.example.budongbudong.domain.payment.enums.PaymentType;
import com.example.budongbudong.domain.payment.event.PaymentCompletedEvent;
import com.example.budongbudong.domain.payment.event.PaymentRequestedEvent;
import com.example.budongbudong.domain.payment.event.RefundRequestDomainEvent;
import com.example.budongbudong.domain.payment.log.enums.LogType;
import com.example.budongbudong.domain.payment.log.service.PaymentLogService;
import com.example.budongbudong.domain.payment.policy.*;
import com.example.budongbudong.domain.payment.repository.PaymentRepository;
import com.example.budongbudong.domain.payment.toss.client.TossPaymentClient;
import com.example.budongbudong.domain.payment.toss.dto.response.TossConfirmResponse;
import com.example.budongbudong.domain.payment.toss.enums.PaymentFailureReason;
import com.example.budongbudong.domain.payment.toss.exception.TossClientException;
import com.example.budongbudong.domain.payment.toss.exception.TossNetworkException;
import com.example.budongbudong.domain.payment.utils.PaymentAmountCalculator;
import com.example.budongbudong.domain.payment.utils.PaymentMethodDetailFormatter;
import com.example.budongbudong.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final PaymentLogService paymentLogService;
    private final PaymentAmountCalculator calculator;
    private final TossPaymentClient tossPaymentClient;
    private final PaymentReconfirmPublisher reconfirmPublisher;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AuctionWinnerRepository auctionWinnerRepository;
    private final PaymentPolicyFactory factory;

    @Transactional
    public PaymentTossReadyResponse requestPayment(Long userId, Long auctionId, PaymentType type) {

        Auction auction = auctionRepository.getAuctionWithPropertyOrTrow(auctionId);
        User user = userRepository.getByIdOrThrow(userId);

        paymentRepository.findByAuctionAndTypeAndStatus(auction, type, PaymentStatus.SUCCESS)
                .ifPresent(p -> {
                    throw new CustomException(ErrorCode.ALREADY_PAID);
                });

        Optional<Payment> inProgressPayment = paymentRepository.findByAuctionAndTypeAndStatus(auction, type, PaymentStatus.IN_PROGRESS);
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
    public void confirmPayment(Long userId, PaymentConfirmRequest request) {

        Payment payment = paymentRepository.getByOrderIdOrThrow(request.orderId());

        // 소유권 검증
        if (!payment.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        //이미 확정된 결제 중복 처리 방지
        if (payment.isFinalized()) return;

        // 결제 확인 중인 상태는 confirm 호출 차단
        if (payment.getStatus().equals(PaymentStatus.VERIFYING)) return;

        PaymentStatus prev = payment.getStatus();

        //금액 검증
        if (payment.getAmount().compareTo(request.amount()) != 0) {
            payment.makeFail(PaymentFailureReason.AMOUNT_MISMATCH);
            saveLog(payment, prev, PaymentStatus.FAIL ,LogType.STATUS_CHANGE, "금액 불일치");
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        //toss 승인 호출
        TossConfirmResponse tossResponse;
        try {
            tossResponse = tossPaymentClient.confirm(request.paymentKey(), request.orderId(), request.amount());
        } catch (TossClientException e) {
            payment.makeFail(PaymentFailureReason.INVALID_PAYMENT_INFO);
            saveLog(payment, prev, PaymentStatus.FAIL ,LogType.TOSS_CLIENT_ERROR, e.getMessage());
            return;
        } catch (TossNetworkException e) {
            makeVerifyingAndPublish(payment, request, PaymentFailureReason.PG_NETWORK_ERROR);
            saveLog(payment, prev, PaymentStatus.VERIFYING ,LogType.TOSS_NETWORK_ERROR, e.getMessage());
            return;
        }

        // 승인 확정 -> SUCCESS 상태 전이
        try {
            PaymentMethodType methodType = PaymentMethodType.from(tossResponse.getMethod());
            String methodDetail = PaymentMethodDetailFormatter.format(methodType, tossResponse);

            finalizePayment(payment, request);
            payment.makeSuccess(request.paymentKey(), LocalDateTime.now(), methodType, methodDetail);
            saveLog(payment, prev, PaymentStatus.SUCCESS ,LogType.PAYMENT_SUCCESS, null);

            // 결제 완료 알림
            applicationEventPublisher.publishEvent(new PaymentCompletedEvent(payment.getId(), payment.getUser().getId()));

            // 계약금 납부 시 잔금 납부 알림
            if (payment.getType().equals(PaymentType.DOWN_PAYMENT)) {
                applicationEventPublisher.publishEvent(new PaymentRequestedEvent(payment.getAuction().getId(), payment.getUser().getId(), PaymentType.BALANCE, LocalDate.now()));
            }
        } catch (Exception e) {
            makeVerifyingAndPublish(payment, request, PaymentFailureReason.SERVER_CONFIRM_ERROR);
            saveLog(payment, prev, PaymentStatus.VERIFYING ,LogType.STATUS_CHANGE, "서버 확인 오류: " + e.getMessage());
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
        reconfirmPublisher.publish(payment.getId(), 30000L);
    }

    @Transactional(readOnly = true)
    public ReadPaymentResponse getPaymentDetail(Long userId, Long paymentId) {

        ReadPaymentDetailDto dto = paymentRepository.findDetailById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!dto.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return ReadPaymentResponse.from(dto);
    }

    /**
     * 시스템 수동 환불 (자동 환불 처리 실패 시)
     */
    @Transactional
    public void requestRefundByUser(Long userId, Long paymentId) {

        Payment payment = paymentRepository.getByIdAndUserIdOrThrow(paymentId, userId);
        PaymentStatus prev = payment.getStatus();

        payment.requestRefund();

        saveLog(payment, prev, PaymentStatus.REFUND_REQUESTED ,LogType.MANUAL_REFUND_REQUESTED, null);
        applicationEventPublisher.publishEvent(new RefundRequestDomainEvent(paymentId));
    }

    /**
     * 시스템 자동 환불 (경매 종료 시 낙찰 실패자 보증금 환불)
     */
    @Transactional
    public void requestRefund(Long paymentId) {

        Payment payment = paymentRepository.getByIdOrThrow(paymentId);
        PaymentStatus prev = payment.getStatus();

        payment.requestRefund();

        saveLog(payment, prev, PaymentStatus.REFUND_REQUESTED, LogType.REFUND_REQUESTED, null);
        applicationEventPublisher.publishEvent(new RefundRequestDomainEvent(paymentId));
    }

    @Transactional(readOnly = true)
    public PaymentInfoResponse getPaymentInfo(
            Long auctionId,
            Long userId,
            PaymentType type
    ) {

        Auction auction = auctionRepository.getByIdOrThrow(auctionId);
        AuctionWinner winner = auctionWinnerRepository.getAuctionWinnerOrThrow(auctionId);

        PaymentPolicy policy = factory.get(type);

        PaymentInfo info = policy.assembleInfo(auction, winner, userId);

        return new PaymentInfoResponse(
                auction.getId(),
                auction.getProperty().getName(),
                type,
                winner.getPrice(),
                info.payableAmount(),
                info.alreadyPaidAmount(),
                info.rate(),
                auction.getStartedAt(),
                winner.getCreatedAt(),
                info.dueAt()
        );
    }

    /**
     * 계약금, 잔금 결제가 필요한 경매에 대한 결제 요청 목록 조회
     */
    @Transactional(readOnly = true)
    public List<MyPaymentListResponse> getMyRequiredPaymentsByType(Long userId, PaymentType type) {

        if (type != PaymentType.DOWN_PAYMENT && type != PaymentType.BALANCE) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_TYPE);
        }

        List<RequiredPaymentDto> raws = paymentRepository.findRequiredPaymentsByUserId(userId, type);

        return raws.stream()
                .map(raw -> convertToResponse(raw, type))
                .toList();
    }

    /**
     * PG 승인 이후,서버가 결제 결과를 "확정"하는 단계
     * - 지금은 확정의 경계 역할만 한다.
     */
    private void finalizePayment(Payment payment, PaymentConfirmRequest request) {

        paymentRepository.save(payment);
    }

    private void saveLog(Payment payment, PaymentStatus prev, PaymentStatus current, LogType type, String errorMessage) {
        log.info("[결제 로그] paymentId={}, {} -> {}, type={}, error={}", payment.getId(), prev, current, type, errorMessage);
        paymentLogService.saveLog(payment.getId(), prev, payment.getStatus(), type, errorMessage);
    }

    /**
     * 금액 계산용 메서드
     */
    private MyPaymentListResponse convertToResponse(RequiredPaymentDto dto, PaymentType type) {
        log.info("현재 type = {}", type);
        BigDecimal paidAmount =
                dto.getPaidAmount() == null
                        ? BigDecimal.ZERO
                        : dto.getPaidAmount();

        BigDecimal payableAmount;
        LocalDateTime dueAt;

        if (type == PaymentType.DOWN_PAYMENT) {

            // 계약금 = 낙찰가의 10%
            payableAmount = dto.getFinalPrice()
                    .multiply(BigDecimal.valueOf(0.1))
                    .setScale(0, RoundingMode.DOWN);

            // 낙찰 시점 + 24시간
            dueAt = dto.getWinnerCreatedAt().plusHours(24);

        } else {

            // 잔금 = 낙찰가 - (보증금 + 계약금)
            payableAmount = dto.getFinalPrice().subtract(paidAmount);

            if (dto.getDownPaymentApprovedAt() == null) {
                throw new CustomException(ErrorCode.DOWN_PAYMENT_REQUIRED_FIRST);
            }

            // 계약금 승인 시점 + 7일
            dueAt = dto.getDownPaymentApprovedAt().plusDays(7);
        }

        boolean expired = LocalDateTime.now().isAfter(dueAt);

        return new MyPaymentListResponse(
                dto.getAuctionId(),
                dto.getAuctionName(),
                dto.getFinalPrice(),
                payableAmount,
                dueAt,
                expired
        );
    }

}
