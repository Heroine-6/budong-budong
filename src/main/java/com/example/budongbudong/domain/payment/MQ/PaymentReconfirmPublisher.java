package com.example.budongbudong.domain.payment.MQ;

import com.example.budongbudong.domain.payment.config.PaymentVerifyMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentReconfirmPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 결제 재확인 트리거 메시지 발행
     * - delayMillis 이후 Consumer가 처리
     */
    public void publish(Long paymentId, Long delayMillis) {
        PaymentVerifyMessage message = new PaymentVerifyMessage(paymentId);

        rabbitTemplate.convertAndSend(
                PaymentVerifyMQConfig.VERIFY_DELAY_QUEUE,
                message,
                m -> {
                    // 메세지 단위 TTL 설정
                    m.getMessageProperties().setExpiration(String.valueOf(delayMillis));
                    return m;
                }
        );
    }
}
