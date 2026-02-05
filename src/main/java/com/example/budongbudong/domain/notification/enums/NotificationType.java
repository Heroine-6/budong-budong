package com.example.budongbudong.domain.notification.enums;

import lombok.Getter;

@Getter
public enum NotificationType {

    AUCTION_START("경매 시작", "[%s] \\n경매가 시작되었습니다."),
    AUCTION_END_SOON("경매 종료 임박", "[%s] \\n경매가 1시간 뒤에 종료됩니다."),
    AUCTION_END("경매 종료", "[%s] \\n경매가 종료되었습니다."),
    BID_UPDATE("입찰 갱신", "[%s] \\n경매에 새로운 입찰이 발생했습니다"),
    PAYMENT_REQUEST("결제 요청", "[%s] \\n%s 결제가 요청되었습니다. \\n\\n결제 금액: %s\\n결제 기한: %s 까지"),
    PAYMENT_COMPLETED("결제 완료", "[%s] \\n%s 결제가 완료되었습니다.\\n\\n주문 번호: %s\\n결제 금액: %s\\n결제일시: %s");

    private final String title;
    private final String message;

    NotificationType(String title, String message) {
        this.title = title;
        this.message = message;
    }

    public String format(Object... args) {
        return String.format(message, args);
    }
}
