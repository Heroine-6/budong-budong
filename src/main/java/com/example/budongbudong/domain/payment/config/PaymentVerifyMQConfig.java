package com.example.budongbudong.domain.payment.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentVerifyMQConfig {

    public static final String VERIFY_EXCHANGE = "payment.verify.exchange";
    public static final String VERIFY_QUEUE = "payment.verify.queue";
    public static final String VERIFY_DELAY_QUEUE = "payment.verify.delay.queue";
    public static final String VERIFY_ROUTING_KEY = "payment.verify";

    /**
     * 결제 재확인 Exchange
     */
    @Bean
    public DirectExchange paymentVerifyExchange() {
        return new DirectExchange(VERIFY_EXCHANGE);
    }

    /**
     * 실제 Consumer가 소비하는 큐
     */
    @Bean
    public Queue paymentVerifyQueue() {
        return QueueBuilder.durable(VERIFY_QUEUE).build();
    }

    /**
     * Exchange와 Queue 바인딩
     */
    @Bean
    public Binding paymentVerifyBinding(DirectExchange paymentVerifyExchange, Queue paymentVerifyQueue) {
        return BindingBuilder
                .bind(paymentVerifyQueue)
                .to(paymentVerifyExchange)
                .with(VERIFY_ROUTING_KEY);
    }

    /**
     * Delay Queue (TTL → DLX → verify queue)
     */
    @Bean
    public Queue paymentVerifyDelayQueue() {
        return QueueBuilder.durable(VERIFY_DELAY_QUEUE)
                .deadLetterExchange(VERIFY_EXCHANGE)
                .deadLetterRoutingKey(VERIFY_ROUTING_KEY)
                .build();
    }
}
