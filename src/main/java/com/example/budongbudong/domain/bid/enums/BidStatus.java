package com.example.budongbudong.domain.bid.enums;

import lombok.Getter;

@Getter
public enum BidStatus {
    PLACED("입찰이 정상적으로 제출된 상태"),
    OUTBID("다른 사용자의 더 높은 입찰로 밀린 상태"),
    WINNING("현재 최고가 입찰 상태"),
    WON("경매 종료 후 최종 낙찰된 상태"),
    LOST("경매 종료 후 낙찰에 실패한 상태");

    private final String description;

    BidStatus(String description) {
        this.description = description;
    }

}
