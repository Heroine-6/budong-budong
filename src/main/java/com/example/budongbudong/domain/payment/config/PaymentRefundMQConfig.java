package com.example.budongbudong.domain.payment.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentRefundMQConfig {

    public static final String REFUND_EXCHANGE = "refund.exchange";
    public static final String REFUND_QUEUE = "refund.deposit.queue";
    public static final String REFUND_DELAY_QUEUE = "refund.deposit.delay.queue";
    public static final String REFUND_ROUTING_KEY = "refund.deposit";

    /**
     * 환불 Exchange
     */
    @Bean
    public DirectExchange refundExchange() {
        return new DirectExchange(REFUND_EXCHANGE);
    }

    /**
     * 환불 Consumer Queue
     */
    @Bean
    public Queue refundQueue() {
        return QueueBuilder.durable(REFUND_QUEUE).build();
    }

    /**
     * Exchange - Queue Binding
     */
    @Bean
    public Binding refundBinding(DirectExchange refundExchange, Queue refundQueue) {
        return BindingBuilder
                .bind(refundQueue)
                .to(refundExchange)
                .with(REFUND_ROUTING_KEY);
    }

    /**
     * Delay Queue (재시도용)
     */
    @Bean
    public Queue refundDelayQueue() {
        return QueueBuilder.durable(REFUND_DELAY_QUEUE)
                .deadLetterExchange(REFUND_EXCHANGE)
                .deadLetterRoutingKey(REFUND_ROUTING_KEY)
                .build();
    }
}

