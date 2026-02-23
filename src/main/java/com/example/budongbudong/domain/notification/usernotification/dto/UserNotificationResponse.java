package com.example.budongbudong.domain.notification.usernotification.dto;

import com.example.budongbudong.common.entity.UserNotification;
import com.example.budongbudong.domain.notification.enums.NotificationType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class UserNotificationResponse {

    private final Long id;
    private final String content;
    private final NotificationType type;
    private final Long auctionId;
    private final LocalDateTime sendAt;
    private final LocalDateTime createdAt;

    public static UserNotificationResponse from(UserNotification userNotification) {
        return new UserNotificationResponse(
                userNotification.getId(),
                userNotification.getNotification().getContent(),
                userNotification.getNotification().getType(),
                userNotification.getNotification().getAuction().getId(),
                userNotification.getSendAt(),
                userNotification.getCreatedAt()
        );
    }
}
