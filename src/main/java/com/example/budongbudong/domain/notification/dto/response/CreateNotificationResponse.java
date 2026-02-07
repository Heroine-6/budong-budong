package com.example.budongbudong.domain.notification.dto.response;

import com.example.budongbudong.common.entity.Notification;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateNotificationResponse {

    private final Long id;
    private final Long sellerId;
    private final String content;

    public static CreateNotificationResponse from(Notification notification) {
        return new CreateNotificationResponse(
                notification.getId(),
                notification.getSellerId(),
                notification.getContent()
        );
    }
}
