package com.example.budongbudong.domain.notification.enums;

import lombok.Getter;

@Getter
public enum NotificationType {
    AUCTION_START("경매 시작"),
    AUCTION_END("경매 종료"),
    BID_UPDATE("입찰 갱신"),
    PAYMENT("결제");

    private final String message;

    NotificationType(String message) {
        this.message = message;
    }
}
