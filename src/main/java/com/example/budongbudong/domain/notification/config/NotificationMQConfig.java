package com.example.budongbudong.domain.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationMQConfig {

    // Exchange
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String NOTIFICATION_DLX = "notification.dlx";

    // Queue
    public static final String AUCTION_OPEN_QUEUE = "auction.open.queue";
    public static final String AUCTION_ENDING_SOON_QUEUE = "auction.ending.soon.queue";
    public static final String AUCTION_CLOSED_QUEUE = "auction.closed.queue";
    public static final String BID_CREATED_QUEUE = "bid.created.queue";
    public static final String PAYMENT_REQUESTED_QUEUE = "payment.requested.queue";
    public static final String PAYMENT_COMPLETED_QUEUE = "payment.completed.queue";

    public static final String NOTIFICATION_SEND_QUEUE = "notification.send.queue";
    public static final String NOTIFICATION_SEND_DELAY_QUEUE = "notification.send.delay.queue";

    public static final String NOTIFICATION_DLQ = "notification.dlq";

    // Routing Key
    public static final String AUCTION_OPEN_KEY = "auction.open";
    public static final String AUCTION_ENDING_SOON_KEY = "auction.ending.soon";
    public static final String AUCTION_CLOSED_KEY = "auction.closed";
    public static final String BID_CREATED_KEY = "bid.created";
    public static final String PAYMENT_REQUESTED_KEY = "payment.requested";
    public static final String PAYMENT_COMPLETED_KEY = "payment.completed";

    public static final String NOTIFICATION_SEND_KEY = "notification.send";
    public static final String NOTIFICATION_SEND_DELAY_KEY = "notification.delay.send";

    /**
     * Exchange
     */
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange notificationDlx() {
        return new DirectExchange(NOTIFICATION_DLX, true, false);
    }

    /**
     * Queue + DLX
     */
    @Bean
    public Queue auctionOpenQueue() {
        return QueueBuilder.durable(AUCTION_OPEN_QUEUE)
                .deadLetterExchange(NOTIFICATION_DLX)
                .deadLetterRoutingKey("failed")
                .build();
    }

    @Bean
    public Queue auctionEndingSoonQueue() {
        return QueueBuilder.durable(AUCTION_ENDING_SOON_QUEUE)
                .deadLetterExchange(NOTIFICATION_DLX)
                .deadLetterRoutingKey("failed")
                .build();
    }

    @Bean
    public Queue auctionClosedQueue() {
        return QueueBuilder.durable(AUCTION_CLOSED_QUEUE)
                .deadLetterExchange(NOTIFICATION_DLX)
                .deadLetterRoutingKey("failed")
                .build();
    }

    @Bean
    public Queue bidCreatedQueue() {
        return QueueBuilder.durable(BID_CREATED_QUEUE)
                .deadLetterExchange(NOTIFICATION_DLX)
                .deadLetterRoutingKey("failed")
                .build();
    }

    @Bean
    public Queue paymentRequestedQueue() {
        return QueueBuilder.durable(PAYMENT_REQUESTED_QUEUE)
                .deadLetterExchange(NOTIFICATION_DLX)
                .deadLetterRoutingKey("failed")
                .build();
    }

    @Bean
    public Queue paymentCompletedQueue() {
        return QueueBuilder.durable(PAYMENT_COMPLETED_QUEUE)
                .deadLetterExchange(NOTIFICATION_DLX)
                .deadLetterRoutingKey("failed")
                .build();
    }

    @Bean
    public Queue notificationSendQueue() {
        return QueueBuilder.durable(NOTIFICATION_SEND_QUEUE)
                .deadLetterExchange(NOTIFICATION_DLX)
                .deadLetterRoutingKey("failed")
                .build();
    }

    @Bean
    public Queue notificationSendDelayQueue() {
        return QueueBuilder.durable(NOTIFICATION_SEND_DELAY_QUEUE)
                .deadLetterExchange(NOTIFICATION_EXCHANGE)
                .deadLetterRoutingKey(NOTIFICATION_SEND_KEY)
                .build();
    }

    @Bean
    public Queue notificationDlq() {
        return QueueBuilder.durable(NOTIFICATION_DLQ).build();
    }

    /**
     * Exchange - Queue Binding
     */
    @Bean
    public Binding auctionOpenBinding() {
        return BindingBuilder
                .bind(auctionOpenQueue())
                .to(notificationExchange())
                .with(AUCTION_OPEN_KEY);
    }

    @Bean
    public Binding auctionEndingSoonBinding() {
        return BindingBuilder
                .bind(auctionEndingSoonQueue())
                .to(notificationExchange())
                .with(AUCTION_ENDING_SOON_KEY);
    }

    @Bean
    public Binding auctionClosedBinding() {
        return BindingBuilder
                .bind(auctionClosedQueue())
                .to(notificationExchange())
                .with(AUCTION_CLOSED_KEY);
    }

    @Bean
    public Binding bidCreatedBinding() {
        return BindingBuilder
                .bind(bidCreatedQueue())
                .to(notificationExchange())
                .with(BID_CREATED_KEY);
    }

    @Bean
    public Binding paymentRequestedBinding() {
        return BindingBuilder
                .bind(paymentRequestedQueue())
                .to(notificationExchange())
                .with(PAYMENT_REQUESTED_KEY);
    }

    @Bean
    public Binding paymentCompletedBinding() {
        return BindingBuilder
                .bind(paymentCompletedQueue())
                .to(notificationExchange())
                .with(PAYMENT_COMPLETED_KEY);
    }

    @Bean
    public Binding notificationSendBinding() {
        return BindingBuilder
                .bind(notificationSendQueue())
                .to(notificationExchange())
                .with(NOTIFICATION_SEND_KEY);
    }

    @Bean
    public Binding notificationSendDelayBinding() {
        return BindingBuilder
                .bind(notificationSendDelayQueue())
                .to(notificationExchange())
                .with(NOTIFICATION_SEND_DELAY_KEY);
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder
                .bind(notificationDlq())
                .to(notificationDlx())
                .with("failed");
    }
}