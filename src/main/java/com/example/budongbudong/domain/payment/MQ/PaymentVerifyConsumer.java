package com.example.budongbudong.domain.payment.MQ;

import com.example.budongbudong.common.entity.Payment;
import com.example.budongbudong.domain.payment.config.PaymentVerifyMQConfig;
import com.example.budongbudong.domain.payment.toss.enums.PaymentFailureReason;
import com.example.budongbudong.domain.payment.enums.PaymentStatus;
import com.example.budongbudong.domain.payment.repository.PaymentRepository;
import com.example.budongbudong.domain.payment.toss.client.TossPaymentClient;
import com.example.budongbudong.domain.payment.toss.dto.response.TossPaymentStatusResponse;
import com.example.budongbudong.domain.payment.toss.enums.TossPaymentStatus;
import com.example.budongbudong.domain.payment.toss.exception.TossNetworkException;
import com.example.budongbudong.domain.payment.toss.utils.TossPaymentStatusMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class PaymentVerifyConsumer {

    private static final Duration VERIFY_LIMIT = Duration.ofMinutes(3); //VERIFYING 상태 유지 시간

    private final PaymentRepository paymentRepository;
    private final TossPaymentClient tossPaymentClient;
    private final PaymentVerifyPublisher verifyPublisher;
    private final TossPaymentStatusMapper mapper;

    @RabbitListener(
            queues = PaymentVerifyMQConfig.VERIFY_QUEUE,
            containerFactory = "simpleRabbitListenerContainerFactory"
    )
    @Transactional
    public void consume(PaymentVerifyMessage message) {

        Payment payment = paymentRepository.getByIdOrThrow(message.paymentId());

        // 중복 consume 방지
        if(payment.isFinalized()) {
            return;
        }

        // VERIFYING 상태만 재확인 대상
        if(payment.getStatus() != PaymentStatus.VERIFYING) {
            return;
        }
        // VERIFYING 타임아웃 -> 즉시 FAIL
        if(payment.isVerifiedTimeout(VERIFY_LIMIT)) {
            payment.makeFail(PaymentFailureReason.PG_TIMEOUT);
            return;
        }

        try {
            // PG 상태 재조회
            TossPaymentStatusResponse response = tossPaymentClient.getPayment(payment.getPaymentKey());
            TossPaymentStatus tossStatus = mapper.map(response);

            if(tossStatus.isFinalized()) {
                payment.finalizeByTossStatus(tossStatus, null, null);
            } else {
                requeue(payment);
            }
        } catch(TossNetworkException e) {
            requeue(payment);
        }
    }
    private void requeue (Payment payment) {
        //PG 조회 자체가 실패한 경우 재시도
        verifyPublisher.publish(payment.getId(), 30000L);
    }
}
