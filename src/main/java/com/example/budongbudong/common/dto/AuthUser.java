package com.example.budongbudong.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthUser {

    private final Long userId;
    private final String email;
    private final String name;

    public static AuthUser of(Long userId, String email, String name) {
        return new AuthUser(userId, email, name);
    }
}
