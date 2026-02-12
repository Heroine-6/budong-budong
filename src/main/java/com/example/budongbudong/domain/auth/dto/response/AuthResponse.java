package com.example.budongbudong.domain.auth.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AuthResponse {
    private final String accessToken;
    private final String refreshToken;
    private final boolean isProfileComplete;

    public AuthResponse(String accessToken, String refreshToken) {
        this(accessToken, refreshToken, true);
    }
}
