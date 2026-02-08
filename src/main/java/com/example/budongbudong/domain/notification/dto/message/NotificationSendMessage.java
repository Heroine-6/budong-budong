package com.example.budongbudong.domain.notification.dto.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSendMessage {

    private String content;
    private Long userNotificationId;
    private int retryCount;

    public static NotificationSendMessage from(String content, Long userNotificationId) {
        return new NotificationSendMessage(
                content,
                userNotificationId,
                0
        );
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }
}