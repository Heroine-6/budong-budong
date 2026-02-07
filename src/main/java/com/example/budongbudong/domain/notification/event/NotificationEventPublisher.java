package com.example.budongbudong.domain.notification.event;

import com.example.budongbudong.domain.auction.event.AuctionClosedEvent;
import com.example.budongbudong.domain.auction.event.AuctionEndingSoonEvent;
import com.example.budongbudong.domain.auction.event.AuctionOpenEvent;
import com.example.budongbudong.domain.bid.event.BidCreatedEvent;
import com.example.budongbudong.domain.notification.config.NotificationMQConfig;
import com.example.budongbudong.domain.notification.dto.message.*;
import com.example.budongbudong.domain.payment.event.PaymentCompletedEvent;
import com.example.budongbudong.domain.payment.event.PaymentRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 도메인 이벤트를 RabbitMQ로 발행하는 Publisher
 * 트랜잭션 커밋 후 메시지 큐로 이벤트 전송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 경매 시작 이벤트 발행
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishAuctionOpen(AuctionOpenEvent event) {
        log.info("경매 시작 이벤트 발행 - auctionId={}", event.auctionId());

        AuctionOpenMessage message = AuctionOpenMessage.from(event);

        rabbitTemplate.convertAndSend(
                NotificationMQConfig.NOTIFICATION_EXCHANGE,
                NotificationMQConfig.AUCTION_OPEN_KEY,
                message
        );
    }

    /**
     * 경매 종료 임박 이벤트 발행
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishAuctionEndingSoon(AuctionEndingSoonEvent event) {
        log.info("경매 종료 임박 이벤트 발행 - auctionId={}", event.auctionId());

        AuctionEndingSoonMessage message = AuctionEndingSoonMessage.from(event);

        rabbitTemplate.convertAndSend(
                NotificationMQConfig.NOTIFICATION_EXCHANGE,
                NotificationMQConfig.AUCTION_ENDING_SOON_KEY,
                message
        );
    }

    /**
     * 경매 종료 이벤트 발행
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishAuctionClosed(AuctionClosedEvent event) {
        log.info("경매 종료 이벤트 발행 - auctionId={}", event.auctionId());

        AuctionClosedMessage message = AuctionClosedMessage.from(event);

        rabbitTemplate.convertAndSend(
                NotificationMQConfig.NOTIFICATION_EXCHANGE,
                NotificationMQConfig.AUCTION_CLOSED_KEY,
                message
        );
    }

    /**
     * 입찰 생성 이벤트 발행
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishBidCreated(BidCreatedEvent event) {
        log.info(
                "입찰 생성 이벤트 발행 - auctionId={}, bidderId={}",
                event.auctionId(),
                event.bidderId()
        );

        BidCreatedMessage message = BidCreatedMessage.from(event);

        rabbitTemplate.convertAndSend(
                NotificationMQConfig.NOTIFICATION_EXCHANGE,
                NotificationMQConfig.BID_CREATED_KEY,
                message
        );
    }

    /**
     * 결제 요청 이벤트 발행
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishPaymentRequested(PaymentRequestedEvent event) {
        log.info(
                "결제 요청 이벤트 발행 - auctionId={}, userId={}",
                event.auctionId(),
                event.userId()
        );

        PaymentRequestedMessage message = PaymentRequestedMessage.from(event);

        rabbitTemplate.convertAndSend(
                NotificationMQConfig.NOTIFICATION_EXCHANGE,
                NotificationMQConfig.PAYMENT_REQUESTED_KEY,
                message
        );
    }

    /**
     * 결제 완료 이벤트 발행
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        log.info(
                "결제 완료 이벤트 발행 - paymentId={}, userId={}",
                event.paymentId(),
                event.userId()
        );

        PaymentCompletedMessage message = PaymentCompletedMessage.from(event);

        rabbitTemplate.convertAndSend(
                NotificationMQConfig.NOTIFICATION_EXCHANGE,
                NotificationMQConfig.PAYMENT_COMPLETED_KEY,
                message
        );
    }
}