package com.example.budongbudong.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record KakaoProfileCompleteRequest(

        @NotBlank(message = "이름은 필수 입력값입니다.")
        String name,

        @NotBlank(message = "전화번호는 필수 입력값입니다.")
        String phone,

        @NotBlank(message = "주소는 필수 입력값입니다.")
        String address
) {
}
