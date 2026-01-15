package com.example.budongbudong.domain.auth.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignUpRequest {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 50, message = "이름은 최대 50자까지 입력 가능합니다.")
    private String name;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/]).*$",
            message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 하나 이상 포함해야 합니다."
    )
    private String password;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Size(max = 50, message = "전화번호는 최대 50자까지 입력 가능합니다.")
    @Pattern(
            regexp = "^[0-9]+$",
            message = "전화번호는 하이픈(-) 없이 숫자만 입력해주세요."
    )
    private String phone;

    @NotBlank(message = "주소는 필수입니다.")
    @Size(max = 100, message = "주소는 최대 100자까지 입력 가능합니다.")
    private String address;

    @NotBlank(message = "역할은 필수입니다.")
    private String role;
}
