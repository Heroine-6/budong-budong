package com.example.budongbudong.domain.notification.enums;

import lombok.Getter;

@Getter
public enum NotificationType {

    AUCTION_START("경매 시작", "[%s] 경매가 시작되었습니다."),
    AUCTION_END_SOON("경매 종료 임박", "[%s] 경매가 1시간 뒤에 종료됩니다."),
    AUCTION_END("경매 종료", "[%s] 경매가 종료되었습니다."),
    BID_UPDATE("입찰 갱신", "[%s] 경매에 새로운 입찰이 발생했습니다"),
    // TODO: 결제 알림 타입 추가
    PAYMENT("결제", ""),
    PAYMENT_COMPLETED("결제 완료", "결제가 완료되었습니다.");

    private final String title;
    private final String message;

    NotificationType(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public String format(String auctionName) {
        return String.format(message, auctionName);
    }
}
