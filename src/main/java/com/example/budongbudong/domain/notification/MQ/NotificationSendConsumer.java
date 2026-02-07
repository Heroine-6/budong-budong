package com.example.budongbudong.domain.notification.MQ;

import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.domain.notification.config.NotificationMQConfig;
import com.example.budongbudong.domain.notification.dto.message.NotificationSendMessage;
import com.example.budongbudong.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 메시지를 소비하여 알림 발송 처리
 * - 카카오톡 알림 발송
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSendConsumer {

    private final NotificationService notificationService;
    private final NotificationSendPublisher notificationSendPublisher;

    @RabbitListener(
            queues = NotificationMQConfig.NOTIFICATION_SEND_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void sendNotification(NotificationSendMessage message) {
        log.info("[알림 발송] 메시지 수신 - content={}, userNotificationId={}", message.getContent(), message.getUserNotificationId());

        try {
            notificationService.sendMessage(message.getUserNotificationId(), message.getContent());
            log.debug("[알림 발송] 성공 - userNotificationId={}", message.getUserNotificationId());
        } catch (CustomException e) {
            log.error("[알림 발송] 실패 - userNotificationId={}, error={}", message.getUserNotificationId(), e.getMessage());

        } catch (Exception e) {
            log.error("[알림 발송] 실패 - userNotificationId={}, error={}", message.getUserNotificationId(), e.getMessage());

            // 실패 시 재시도 처리
            notificationSendPublisher.retrySend(message);
        }
    }

}
