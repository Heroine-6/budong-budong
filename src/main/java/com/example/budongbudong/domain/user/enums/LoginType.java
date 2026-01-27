package com.example.budongbudong.domain.user.enums;

public enum LoginType {
    KAKAO("카카오"),
    GOOGLE("구글");

    private final String message;

    LoginType(String message) {
        this.message = message;
    }
}
