package com.example.budongbudong.domain.payment.MQ;

import com.example.budongbudong.common.entity.Payment;
import com.example.budongbudong.domain.payment.config.PaymentVerifyMQConfig;
import com.example.budongbudong.domain.payment.enums.PaymentFailureReason;
import com.example.budongbudong.domain.payment.enums.PaymentStatus;
import com.example.budongbudong.domain.payment.repository.PaymentRepository;
import com.example.budongbudong.domain.payment.toss.client.TossPaymentClient;
import com.example.budongbudong.domain.payment.toss.dto.response.TossPaymentStatusResponse;
import com.example.budongbudong.domain.payment.toss.exception.TossNetworkException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class PaymentVerifyConsumer {

    private static final Duration VERIFY_LIMIT = Duration.ofMinutes(3); //VERIFYING 상태 유지 시간

    private final PaymentRepository paymentRepository;
    private final TossPaymentClient tossPaymentClient;
    private final PaymentVerifyPublisher verifyPublisher;
    private static final  String DONE = "DONE";
    private static final  String CANCELED = "CANCELED";
    private static final  String ABORTED = "ABORTED";

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

        if(payment.isVerifiedTimeout(VERIFY_LIMIT)) {
            payment.makeFail(PaymentFailureReason.PG_TIMEOUT);
            return;
        }

        try {
            // PG 상태 재조회
            TossPaymentStatusResponse response = tossPaymentClient.getPayment(payment.getPaymentKey());
            switch(response.getStatus()) {
                case DONE -> payment.makeSuccess(payment.getPaymentKey(), LocalDateTime.now());
                case CANCELED, ABORTED -> payment.makeFail(PaymentFailureReason.INVALID_PAYMENT_INFO);
                default -> verifyPublisher.publish(payment.getId(), 30000L); // 아직 확정 불가
            }
        } catch(TossNetworkException e) {
            //PG 조회 자체가 실패한 경우 재시도
            verifyPublisher.publish(payment.getId(), 30000L);
        }
    }
}
