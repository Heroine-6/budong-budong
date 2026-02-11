package com.example.budongbudong.domain.auction.enums;

import lombok.Getter;

@Getter
public enum AuctionType {
    ENGLISH("영국식 경매"),
    DUTCH("네덜란드식 경매");

    private final String description;

    AuctionType(String description) {
        this.description = description;
    }
}
