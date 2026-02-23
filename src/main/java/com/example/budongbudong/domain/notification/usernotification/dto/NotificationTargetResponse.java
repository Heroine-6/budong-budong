package com.example.budongbudong.domain.notification.usernotification.dto;

import com.example.budongbudong.common.entity.UserNotification;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NotificationTargetResponse {

    private final Long userNotificationId;
    private final boolean isPushAllowed;

    public static NotificationTargetResponse from(UserNotification userNotification) {
        return new NotificationTargetResponse(
                userNotification.getId(),
                userNotification.getUser().isPushAllowed()
        );
    }
}
