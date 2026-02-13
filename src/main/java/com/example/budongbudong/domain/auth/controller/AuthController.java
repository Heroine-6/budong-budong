package com.example.budongbudong.domain.auth.controller;

import com.example.budongbudong.common.dto.AuthUser;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.common.utils.annotation.SecurityNotRequired;
import com.example.budongbudong.domain.auth.dto.request.*;
import com.example.budongbudong.domain.auth.dto.response.AuthResponse;
import com.example.budongbudong.domain.auth.service.AuthService;
import com.example.budongbudong.domain.auth.service.SmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SmsService smsService;

    @SecurityNotRequired
    @Operation(summary = "SMS 인증번호 발송", description = "회원가입 전 휴대폰 번호로 인증번호를 발송합니다.")
    @PostMapping("/v1/send")
    public ResponseEntity<GlobalResponse<Void>> sendAuthCode(@RequestBody SmsSendRequest request) {

        smsService.sendAuthCode(request.toNumber());

        return GlobalResponse.ok(null);
    }

    @SecurityNotRequired
    @Operation(summary = "SMS 인증번호 확인", description = "발송된 인증번호의 일치 여부를 검증합니다.")
    @PostMapping("/v1/verify")
    public ResponseEntity<GlobalResponse<Void>> verifyAuthCode(@RequestBody SmsVerifyRequest request) {

        authService.verifyAuthCode(request.toNumber(), request.code());

        return GlobalResponse.ok(null);
    }

    @SecurityNotRequired
    @Operation(summary = "회원가입", description = "이메일/비밀번호 기반 일반 회원가입입니다. SMS 인증 완료 후 사용 가능합니다.")
    @PostMapping("/v1/signup")
    public ResponseEntity<GlobalResponse<AuthResponse>> signUp(@Valid @RequestBody SignUpRequest request) {

        AuthResponse response = authService.signUp(request);

        return GlobalResponse.ok(response);
    }

    @SecurityNotRequired
    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인합니다. 응답의 accessToken을 Authorize에 입력하세요.")
    @PostMapping("/v1/signin")
    public ResponseEntity<GlobalResponse<AuthResponse>> signIn(@Valid @RequestBody SignInRequest request) {

        AuthResponse response = authService.signIn(request);

        return GlobalResponse.ok(response);
    }

    @SecurityNotRequired
    @Operation(summary = "액세스 토큰 재발급", description = "만료된 accessToken을 refreshToken으로 재발급합니다.")
    @PostMapping("/v1/refresh")
    public ResponseEntity<GlobalResponse<AuthResponse>> reissueAccessToken(@Valid @RequestBody ReissueAccessTokenRequest request) {

        AuthResponse response = authService.reissueAccessToken(request);

        return GlobalResponse.ok(response);
    }

    @SecurityNotRequired
    @Operation(summary = "카카오 소셜 로그인", description = "카카오 인가 코드로 로그인 또는 회원가입합니다.")
    @GetMapping("/v2/kakao")
    public ResponseEntity<GlobalResponse<AuthResponse>> kakaoLogin(@RequestParam String code) {

        AuthResponse response = authService.kakaoLogin(code);

        return GlobalResponse.ok(response);
    }

    @Operation(summary = "카카오 회원 프로필 완성", description = "카카오 소셜 가입 시 누락된 전화번호/주소를 추가로 입력합니다.")
    @PatchMapping("/v2/kakao/complete")
    public ResponseEntity<GlobalResponse<Void>> completeProfile(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody KakaoProfileCompleteRequest request
    ) {
        authService.completeProfile(authUser.getUserId(), request.phone(), request.address());

        return GlobalResponse.ok(null);
    }
}
