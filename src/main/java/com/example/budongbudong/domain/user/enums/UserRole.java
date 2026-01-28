package com.example.budongbudong.domain.user.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    GENERAL("사용자"),
    SELLER("판매자"),
    ADMIN("관리자");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }
}
