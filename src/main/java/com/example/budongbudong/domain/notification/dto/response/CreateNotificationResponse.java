package com.example.budongbudong.domain.notification.dto.response;

import com.example.budongbudong.common.entity.Notification;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateNotificationResponse {

    private final Long id;

    public static CreateNotificationResponse from(Notification notification) {
        return new CreateNotificationResponse(notification.getId());
    }
}
