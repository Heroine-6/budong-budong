package com.example.budongbudong.domain.payment.event;

import com.example.budongbudong.domain.payment.MQ.RefundRequestedMQEvent;
import com.example.budongbudong.domain.payment.config.PaymentRefundMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentRefundPublisher {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(RefundRequestDomainEvent event) {

        rabbitTemplate.convertAndSend(
                PaymentRefundMQConfig.REFUND_EXCHANGE,
                PaymentRefundMQConfig.REFUND_ROUTING_KEY,
                new RefundRequestedMQEvent(event.paymentId())
        );
    }
}
