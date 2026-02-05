package com.example.budongbudong.domain.payment.MQ;

import com.example.budongbudong.common.entity.Payment;
import com.example.budongbudong.domain.payment.config.PaymentRefundMQConfig;
import com.example.budongbudong.domain.payment.enums.PaymentStatus;
import com.example.budongbudong.domain.payment.log.enums.LogType;
import com.example.budongbudong.domain.payment.log.service.PaymentLogService;
import com.example.budongbudong.domain.payment.repository.PaymentRepository;
import com.example.budongbudong.domain.payment.toss.client.TossPaymentClient;
import com.example.budongbudong.domain.payment.toss.exception.TossClientException;
import com.example.budongbudong.domain.payment.toss.exception.TossNetworkException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 환불 MQ 메시지를 소비해 실제 환불을 실행하는 Consumer
 * - Toss 결제 환불 API를 호출한다
 * - 재시도 여부는 예외 타입으로 제어한다
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRefundConsumer {

    private final PaymentRepository paymentRepository;
    private final PaymentLogService paymentLogService;
    private final TossPaymentClient client;

    @RabbitListener(queues = PaymentRefundMQConfig.REFUND_QUEUE)
    @Transactional
    public void consume(RefundRequestedMQEvent event) {

        Payment payment = paymentRepository.getByIdOrThrow(event.getPaymentId());

        if (payment.getStatus() == PaymentStatus.REFUNDED) return;
        executeRefund(payment);
    }

    /* 실제 환불 실행 메서드 */
    private void executeRefund(Payment payment) {
        PaymentStatus prev = payment.getStatus();
        try {
            client.refund(payment.getPaymentKey(), payment.getAmount(), "경매 낙찰 실패에 따른 보증금 환불");
            payment.makeRefunded();
            saveLog(payment, prev, LogType.REFUND_SUCCESS, null);

        } catch (TossClientException e) {
            log.error("환불 요청 거절 - paymentId: {}, 사유: {}", payment.getId(), e.getMessage());
            saveLog(payment, prev, LogType.REFUND_FAILED, e.getMessage());

        } catch (TossNetworkException e) {
            log.warn("환불 네트워크 오류 - paymentId: {}, 재시도 필요", payment.getId());
            saveLog(payment, prev, LogType.REFUND_RETRY, e.getMessage());
            throw e;
        }
    }

    private void saveLog(Payment payment, PaymentStatus prev, LogType type, String errorMessage) {
        paymentLogService.saveLog(payment.getId(), prev, payment.getStatus(), type, errorMessage);
    }

}
