package com.example.budongbudong.domain.payment.MQ;

import com.example.budongbudong.domain.payment.config.PaymentRefundMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentRefundRetryPublisher {

    private static final long DEFAULT_DELAY_MILLIS = 30000L; // 30초

    private final RabbitTemplate rabbitTemplate;

    /**
     * 환불 재시도 메시지 발행
     * - delayMillis 이후 Consumer가 처리
     */
    public void publish(Long paymentId, Long delayMillis) {
        RefundRequestedMQEvent message = new RefundRequestedMQEvent(paymentId);

        rabbitTemplate.convertAndSend(
                PaymentRefundMQConfig.REFUND_DELAY_QUEUE,
                message,
                m -> {
                    m.getMessageProperties().setExpiration(String.valueOf(delayMillis));
                    return m;
                }
        );
    }

    public void publish(Long paymentId) {
        publish(paymentId, DEFAULT_DELAY_MILLIS);
    }
}
