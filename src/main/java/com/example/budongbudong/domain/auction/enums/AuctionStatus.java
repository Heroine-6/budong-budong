package com.example.budongbudong.domain.auction.enums;

import lombok.Getter;

@Getter
public enum AuctionStatus {

    SCHEDULED("경매 시작 전"),
    OPEN("경매 진행 중"),
    CLOSED("경매 정상 종료"),
    FAILED("유찰"),
    CANCELLED("경매 취소");

    private final String description;

    AuctionStatus(String description) {
        this.description = description;
    }
}
