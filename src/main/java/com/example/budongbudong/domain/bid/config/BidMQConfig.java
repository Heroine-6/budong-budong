package com.example.budongbudong.domain.bid.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BidMQConfig {

    public static final String BID_EXCHANGE = "bid.exchange";
    public static final String BID_CREATE_QUEUE = "bid.queue";
    public static final String BID_CREATE_KEY = "bid.created";

    @Bean
    public DirectExchange bidExchange() {
        return new DirectExchange(BID_EXCHANGE);
    }

    @Bean
    public Queue bidQueue() {
        return QueueBuilder.durable(BID_CREATE_QUEUE).build();
    }

    @Bean
    public Binding bidBinding() {
        return BindingBuilder
                .bind(bidQueue())
                .to(bidExchange())
                .with(BID_CREATE_KEY);
    }
}
