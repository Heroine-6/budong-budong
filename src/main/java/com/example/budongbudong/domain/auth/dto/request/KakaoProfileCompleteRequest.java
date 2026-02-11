package com.example.budongbudong.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record KakaoProfileCompleteRequest(
        @NotBlank String phone,
        @NotBlank String address
) {}
