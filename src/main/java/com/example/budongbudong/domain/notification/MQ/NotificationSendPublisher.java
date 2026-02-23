package com.example.budongbudong.domain.notification.MQ;

import com.example.budongbudong.domain.notification.config.NotificationMQConfig;
import com.example.budongbudong.domain.notification.dto.message.NotificationSendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSendPublisher {

    private static final long DEFAULT_DELAY_MILLIS = 30000L; // 30초
    private static final int MAX_RETRY_COUNT = 5; // 최대 재시도 횟수

    private final RabbitTemplate rabbitTemplate;

    /***
     * 알림 발송 메시지 발행
     */
    public void publish(NotificationSendMessage message) {
        log.info("[알림] 발송 메시지 발행 | userNotificationId={}", message.getUserNotificationId());

        rabbitTemplate.convertAndSend(
                NotificationMQConfig.NOTIFICATION_EXCHANGE,
                NotificationMQConfig.NOTIFICATION_SEND_KEY,
                message
        );
    }

    /**
     * 알림 발송 재시도 메시지 발행
     * - delayMillis 이후 Consumer가 처리
     * - 재시도 횟수에 비례하여 delayMillis 증가
     */
    public void retrySend(NotificationSendMessage message) {

        int retryCount = message.getRetryCount();
        if (retryCount >= MAX_RETRY_COUNT) {
            log.error("[알림 발송 재시도] 최대 재시도 초과 - message={}", message);
            rabbitTemplate.convertAndSend(NotificationMQConfig.NOTIFICATION_DLQ, message);
            return;
        }

        message.incrementRetryCount();
        long delayMillis = (long) Math.pow(2, retryCount) * DEFAULT_DELAY_MILLIS;

        rabbitTemplate.convertAndSend(
                NotificationMQConfig.NOTIFICATION_SEND_DELAY_QUEUE,
                message,
                m -> {
                    m.getMessageProperties().setExpiration(String.valueOf(delayMillis));
                    return m;
                }
        );

        log.info("[알림 발송 재시도] delayQueue로 발송 - retryCount={}, delay={}ms", retryCount, delayMillis);
    }
}

