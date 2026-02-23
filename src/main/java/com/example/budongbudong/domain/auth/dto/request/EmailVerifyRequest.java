package com.example.budongbudong.domain.auth.dto.request;

import jakarta.validation.constraints.Email;

public record EmailVerifyRequest(
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email
) {
}