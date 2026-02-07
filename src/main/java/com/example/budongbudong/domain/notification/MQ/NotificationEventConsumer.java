package com.example.budongbudong.domain.notification.MQ;

import com.example.budongbudong.domain.notification.config.NotificationMQConfig;
import com.example.budongbudong.domain.notification.dto.message.*;
import com.example.budongbudong.domain.notification.dto.response.CreateNotificationResponse;
import com.example.budongbudong.domain.notification.enums.NotificationType;
import com.example.budongbudong.domain.notification.service.NotificationService;
import com.example.budongbudong.domain.notification.usernotification.dto.NotificationTargetResponse;
import com.example.budongbudong.domain.notification.usernotification.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RabbitMQ 메시지를 소비하여 알림 생성 및 발송 메시지 발행
 * 1. 알림(Notification) 생성
 * 2. 수신 등록(UserNotification)
 * 3. 알림 발송 메시지 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final UserNotificationService userNotificationService;
    private final NotificationSendPublisher notificationSendPublisher;

    /**
     * 경매 시작 메시지 처리
     * 판매자 대상 알림
     */
    @RabbitListener(
            queues = NotificationMQConfig.AUCTION_OPEN_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleAuctionOpen(AuctionOpenMessage message) {
        log.info("[알림][경매 시작] 메시지 수신 - auctionId={}", message.auctionId());

        try {
            CreateNotificationResponse response = notificationService.createNotification(message.auctionId(), NotificationType.AUCTION_START);

            NotificationTargetResponse target = userNotificationService.createUserNotification(response.getId(), response.getSellerId());

            publishSendMessage(response.getContent(), target);

            log.info("[알림][경매 시작] 알림 생성 완료 - auctionId={}", message.auctionId());

        } catch (Exception e) {
            log.error("[알림][경매 시작] 처리 실패 - auctionId={}, error={}", message.auctionId(), e.getMessage());
        }
    }

    /**
     * 경매 종료 임박 메시지 처리
     * 판매자 + 입찰자 대상 알림
     */
    @RabbitListener(
            queues = NotificationMQConfig.AUCTION_ENDING_SOON_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleAuctionEndingSoon(AuctionEndingSoonMessage message) {
        log.info("[알림][경매 종료 임박] 메시지 수신 - auctionId={}", message.auctionId());

        try {
            CreateNotificationResponse response = notificationService.createNotification(message.auctionId(), NotificationType.AUCTION_END_SOON);

            List<NotificationTargetResponse> targets = userNotificationService.createUserNotificationAllUsers(response.getId(), message.auctionId());

            publishSendMessages(response.getContent(), targets);

            log.info("[알림][경매 종료 임박] 알림 생성 완료 - auctionId={}", message.auctionId());

        } catch (Exception e) {
            log.error("[알림][경매 종료 임박] 처리 실패 - auctionId={}, error={}", message.auctionId(), e.getMessage());
        }
    }

    /**
     * 경매 종료 메시지 처리
     * 판매자 + 입찰자 대상 알림
     */
    @RabbitListener(
            queues = NotificationMQConfig.AUCTION_CLOSED_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleAuctionClosed(AuctionClosedMessage message) {
        log.info("[알림][경매 종료] 메시지 수신 - auctionId={}", message.auctionId());

        try {
            CreateNotificationResponse response = notificationService.createNotification(message.auctionId(), NotificationType.AUCTION_END);

            List<NotificationTargetResponse> targets = userNotificationService.createUserNotificationAllUsers(response.getId(), message.auctionId());

            publishSendMessages(response.getContent(), targets);

            log.info("[알림][경매 종료] 알림 생성 완료 - auctionId={}", message.auctionId());

        } catch (Exception e) {
            log.error("[알림][경매 종료] 처리 실패 - auctionId={}, error={}", message.auctionId(), e.getMessage());
        }
    }

    /**
     * 입찰 생성 메시지 처리
     * 판매자 + 입찰자 대상 알림
     */
    @RabbitListener(
            queues = NotificationMQConfig.BID_CREATED_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleBidCreated(BidCreatedMessage message) {
        log.info("[알림][입찰 생성] 메시지 수신 - auctionId={}, bidderId={}", message.auctionId(), message.bidderId());

        try {
            CreateNotificationResponse response = notificationService.createNotification(message.auctionId(), NotificationType.BID_UPDATE);

            List<NotificationTargetResponse> targets = userNotificationService.createUserNotificationAllUsers(response.getId(), message.auctionId());

            publishSendMessages(response.getContent(), targets);

            log.info("[알림][입찰 생성] 알림 생성 완료 - auctionId={}", message.auctionId());

        } catch (Exception e) {
            log.error("[알림][입찰 생성] 처리 실패 - auctionId={}, error={}", message.auctionId(), e.getMessage());
        }
    }

    /**
     * 결제 요청 메시지 처리
     */
    @RabbitListener(
            queues = NotificationMQConfig.PAYMENT_REQUESTED_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void handlePaymentRequested(PaymentRequestedMessage message) {
        log.info("[알림][결제 요청] 메시지 수신 - auctionId={}, userId={}", message.auctionId(), message.userId());

        try {
            CreateNotificationResponse response =
                    notificationService.createPaymentRequestNotification(
                            message.auctionId(),
                            message.type(),
                            NotificationType.PAYMENT_REQUEST,
                            message.baseDate()
                    );

            NotificationTargetResponse target = userNotificationService.createUserNotification(response.getId(), message.userId());

            publishSendMessage(response.getContent(), target);

            log.info("[알림][결제 요청] 알림 생성 완료 - userId={}", message.userId());

        } catch (Exception e) {
            log.error("[알림][결제 요청] 처리 실패 - userId={}, error={}", message.userId(), e.getMessage());
        }
    }

    /**
     * 결제 완료 메시지 처리
     */
    @RabbitListener(
            queues = NotificationMQConfig.PAYMENT_COMPLETED_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void handlePaymentCompleted(PaymentCompletedMessage message) {
        log.info("[알림][결제 완료] 메시지 수신 - paymentId={}, userId={}", message.paymentId(), message.userId());

        try {
            CreateNotificationResponse response = notificationService.createPaymentCompletedNotification(NotificationType.PAYMENT_COMPLETED, message.paymentId());

            NotificationTargetResponse target = userNotificationService.createUserNotification(response.getId(), message.userId());

            publishSendMessage(response.getContent(), target);

            log.info("[알림][결제 완료] 알림 생성 완료 - userId={}", message.userId());

        } catch (Exception e) {
            log.error("[알림][결제 완료] 처리 실패 - paymentId={}, error={}", message.paymentId(), e.getMessage());
        }
    }

    /**
     * 단일 대상자 발송 메시지 발행
     * isPushAllowed가 false인 대상자는 발송하지 않음
     */
    private void publishSendMessage(String content, NotificationTargetResponse target) {
        if (!target.isPushAllowed()) {
            log.debug("[알림 발송] 푸시 미허용 대상 - userNotificationId={}", target.getUserNotificationId());
            return;
        }

        NotificationSendMessage sendMessage = NotificationSendMessage.from(content, target.getUserNotificationId());
        notificationSendPublisher.publish(sendMessage);
    }

    /**
     * 다수 대상자 발송 메시지 발행 (건별)
     */
    private void publishSendMessages(String content, List<NotificationTargetResponse> targets) {
        log.info("[알림 발송] 대상자 수={}", targets.size());

        targets.forEach(target -> publishSendMessage(content, target));
    }
}