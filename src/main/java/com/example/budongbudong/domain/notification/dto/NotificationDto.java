package com.example.budongbudong.domain.notification.dto;

import com.example.budongbudong.common.entity.Notification;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NotificationDto {

    private final Long notificationId;
    private final String content;

    public static NotificationDto from(Notification notification) {
        return new NotificationDto(notification.getId(), notification.getContent());
    }
}
