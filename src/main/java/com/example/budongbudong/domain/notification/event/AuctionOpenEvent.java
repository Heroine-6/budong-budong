package com.example.budongbudong.domain.notification.event;


import com.example.budongbudong.domain.notification.enums.NotificationType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuctionOpenEvent {

    private final Long auctionId;
    private final NotificationType type;
}
