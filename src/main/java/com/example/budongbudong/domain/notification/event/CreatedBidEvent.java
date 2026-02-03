package com.example.budongbudong.domain.notification.event;

import com.example.budongbudong.domain.notification.enums.NotificationType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreatedBidEvent {

    private final Long auctionId;
    private final Long bidderId;
    private final NotificationType type;
}
