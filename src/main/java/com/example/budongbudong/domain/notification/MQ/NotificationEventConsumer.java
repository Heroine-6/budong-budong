package com.example.budongbudong.domain.notification.MQ;

import com.example.budongbudong.domain.notification.config.NotificationMQConfig;
import com.example.budongbudong.domain.notification.dto.NotificationDto;
import com.example.budongbudong.domain.notification.dto.message.*;
import com.example.budongbudong.domain.notification.dto.response.CreateNotificationResponse;
import com.example.budongbudong.domain.notification.enums.NotificationType;
import com.example.budongbudong.domain.notification.service.NotificationService;
import com.example.budongbudong.domain.notification.usernotification.dto.GetNotificationTargetResponse;
import com.example.budongbudong.domain.notification.usernotification.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RabbitMQ 메시지를 소비하여 알림 생성 및 발송 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final NotificationService notificationService;
    private final UserNotificationService userNotificationService;

    /**
     * 경매 생성 메시지 처리
     */
    @RabbitListener(
            queues = NotificationMQConfig.AUCTION_CREATED_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleAuctionCreated(AuctionCreatedMessage message) {
        log.info("[알림][경매 생성] 메시지 수신 - auctionId={}", message.auctionId());

        try {
            List<NotificationType> types = List.of(
                    NotificationType.AUCTION_START,
                    NotificationType.AUCTION_END_SOON,
                    NotificationType.AUCTION_END,
                    NotificationType.BID_UPDATE
            );

            types.forEach(type -> {
                CreateNotificationResponse response =
                        notificationService.createSellerNotification(
                                message.auctionId(),
                                message.sellerId(),
                                type
                        );

                userNotificationService.createUserNotification(response.getId(), message.sellerId());
            });

            log.info("[알림][경매 생성] 알림 정의 생성 완료 - auctionId={}", message.auctionId());

        } catch (Exception e) {
            log.error("[알림][경매 생성] 처리 실패 - auctionId={}", message.auctionId(), e);
            throw e;
        }
    }

    /**
     * 경매 시작 메시지 처리
     */
    @RabbitListener(
            queues = NotificationMQConfig.AUCTION_OPEN_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleAuctionOpen(AuctionOpenMessage message) {
        log.info("[알림][경매 시작] 메시지 수신 - auctionId={}", message.auctionId());

        try {
            NotificationDto dto = notificationService.getNotification(message.auctionId(), NotificationType.AUCTION_START);

            sendNotification(dto);

            log.info("[알림][경매 시작] 알림 발송 완료 - auctionId={}", message.auctionId());

        } catch (Exception e) {
            log.error("[알림][경매 시작] 처리 실패 - auctionId={}", message.auctionId(), e);
            throw e;
        }
    }

    /**
     * 경매 종료 임박 메시지 처리
     */
    @RabbitListener(
            queues = NotificationMQConfig.AUCTION_ENDING_SOON_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleAuctionEndingSoon(AuctionEndingSoonMessage message) {
        log.info("[알림][경매 종료 임박] 메시지 수신 - auctionId={}", message.auctionId());

        try {
            NotificationDto dto = userNotificationService.createUserNotificationAllBidders(message.auctionId(), NotificationType.AUCTION_END_SOON);

            sendNotification(dto);

            log.info("[알림][경매 종료 임박] 알림 발송 완료 - auctionId={}", message.auctionId());

        } catch (Exception e) {
            log.error("[알림][경매 종료 임박] 처리 실패 - auctionId={}", message.auctionId(), e);
            throw e;
        }
    }

    /**
     * 경매 종료 메시지 처리
     */
    @RabbitListener(
            queues = NotificationMQConfig.AUCTION_CLOSED_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleAuctionClosed(AuctionClosedMessage message) {
        log.info("[알림][경매 종료] 메시지 수신 - auctionId={}", message.auctionId());

        try {
            NotificationDto dto = userNotificationService.createUserNotificationAllBidders(message.auctionId(), NotificationType.AUCTION_END);

            sendNotification(dto);

            log.info("[알림][경매 종료] 알림 발송 완료 - auctionId={}", message.auctionId());

        } catch (Exception e) {
            log.error("[알림][경매 종료] 처리 실패 - auctionId={}", message.auctionId(), e);
            throw e;
        }
    }

    /**
     * 입찰 생성 메시지 처리
     */
    @RabbitListener(
            queues = NotificationMQConfig.BID_CREATED_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void handleBidCreated(BidCreatedMessage message) {
        log.info("[알림][입찰 생성] 메시지 수신 - auctionId={}, bidderId={}", message.auctionId(), message.bidderId());

        try {
            NotificationDto dto =
                    userNotificationService.createUserNotification(
                            message.auctionId(),
                            NotificationType.BID_UPDATE,
                            message.bidderId()
                    );

            sendNotification(dto);

            log.info("[알림][입찰 생성] 알림 발송 완료 - auctionId={}", message.auctionId());

        } catch (Exception e) {
            log.error("[알림][입찰 생성] 처리 실패 - auctionId={}", message.auctionId(), e);
            throw e;
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

            NotificationDto dto = userNotificationService.createUserNotification(response.getId(), message.userId());

            sendNotification(dto);

            log.info("[알림][결제 요청] 알림 발송 완료 - userId={}", message.userId());

        } catch (Exception e) {
            log.error("[알림][결제 요청] 처리 실패 - userId={}", message.userId(), e);
            throw e;
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
            CreateNotificationResponse response =
                    notificationService.createPaymentCompletedNotification(
                            NotificationType.PAYMENT_COMPLETED,
                            message.paymentId()
                    );

            NotificationDto dto =
                    userNotificationService.createUserNotification(
                            response.getId(),
                            message.userId()
                    );

            sendNotification(dto);

            log.info("[알림][결제 완료] 알림 발송 완료 - userId={}", message.userId());

        } catch (Exception e) {
            log.error("[알림][결제 완료] 처리 실패 - paymentId={}", message.paymentId(), e);
            throw e;
        }
    }

    /**
     * 실제 알림 발송 처리
     */
    private void sendNotification(NotificationDto dto) {
        List<GetNotificationTargetResponse> targets = userNotificationService.getNotificationTargets(dto.getNotificationId());

        log.info("[알림 발송] 대상자 수={} notificationId={}", targets.size(), dto.getNotificationId());

        targets.forEach(target -> {
            try {
                notificationService.sendMessage(target.getUserId(), dto.getContent());
                log.debug("[알림 발송] 발송 성공 - userId={}", target.getUserId());
            } catch (Exception e) {
                log.error("[알림 발송] 발송 실패 - userId={}", target.getUserId(), e);
            }
        });
    }
}