package com.example.budongbudong.domain.bid.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BidMQConfig {

    public static final String BID_EXCHANGE = "bid.exchange";
    public static final String BID_CREATE_QUEUE = "bid.create.queue";
    public static final String BID_DELAY_QUEUE = "bid.delay.queue";
    public static final String BID_DLQ = "bid.dlq";
    public static final String BID_CREATE_KEY = "bid.created";

    @Bean
    public DirectExchange bidExchange() {
        return new DirectExchange(BID_EXCHANGE);
    }

    /**
     * 입찰 Consumer Queue (DLQ 라우팅 설정)
     */
    @Bean
    public Queue bidQueue() {
        return QueueBuilder.durable(BID_CREATE_QUEUE)
                .deadLetterExchange(BID_EXCHANGE)
                .deadLetterRoutingKey("bid.failed")
                .build();
    }

    @Bean
    public Binding bidBinding() {
        return BindingBuilder
                .bind(bidQueue())
                .to(bidExchange())
                .with(BID_CREATE_KEY);
    }

    /**
     * Delay Queue (재시도용) - TTL 만료 시 DLX를 통해 bid.queue로 재전송
     */
    @Bean
    public Queue bidDelayQueue() {
        return QueueBuilder.durable(BID_DELAY_QUEUE)
                .deadLetterExchange(BID_EXCHANGE)
                .deadLetterRoutingKey(BID_CREATE_KEY)
                .build();
    }

    /**
     * DLQ - 최대 재시도 초과 메시지 보관
     */
    @Bean
    public Queue bidDlq() {
        return QueueBuilder.durable(BID_DLQ).build();
    }

    @Bean
    public Binding bidDlqBinding() {
        return BindingBuilder
                .bind(bidDlq())
                .to(bidExchange())
                .with("bid.failed");
    }
}
