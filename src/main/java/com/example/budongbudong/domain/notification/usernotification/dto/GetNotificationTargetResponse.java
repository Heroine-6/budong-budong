package com.example.budongbudong.domain.notification.usernotification.dto;

import com.example.budongbudong.common.entity.UserNotification;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class GetNotificationTargetResponse {

    private final Long userId;

    public static GetNotificationTargetResponse from(UserNotification userNotification) {
        return new GetNotificationTargetResponse(userNotification.getUser().getId());
    }
}
