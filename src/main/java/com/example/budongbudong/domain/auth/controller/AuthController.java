package com.example.budongbudong.domain.auth.controller;

import com.example.budongbudong.common.dto.AuthUser;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.auth.dto.request.*;
import com.example.budongbudong.domain.auth.dto.response.AuthResponse;
import com.example.budongbudong.domain.auth.service.AuthService;
import com.example.budongbudong.domain.auth.service.SmsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SmsService smsService;

    @PostMapping("/v1/send")
    public ResponseEntity<GlobalResponse<Void>> sendAuthCode(@RequestBody SmsSendRequest request) {

        smsService.sendAuthCode(request.toNumber());

        return GlobalResponse.ok(null);
    }

    @PostMapping("/v1/verify")
    public ResponseEntity<GlobalResponse<Void>> verifyAuthCode(@RequestBody SmsVerifyRequest request) {

        authService.verifyAuthCode(request.toNumber(), request.code());

        return GlobalResponse.ok(null);
    }

    @PostMapping("/v1/signup")
    public ResponseEntity<GlobalResponse<AuthResponse>> signUp(@Valid @RequestBody SignUpRequest request) {

        AuthResponse response = authService.signUp(request);

        return GlobalResponse.ok(response);
    }

    @PostMapping("/v1/signin")
    public ResponseEntity<GlobalResponse<AuthResponse>> signIn(@Valid @RequestBody SignInRequest request) {

        AuthResponse response = authService.signIn(request);

        return GlobalResponse.ok(response);
    }

    @PostMapping("/v1/refresh")
    public ResponseEntity<GlobalResponse<AuthResponse>> reissueAccessToken(@Valid @RequestBody ReissueAccessTokenRequest request) {

        AuthResponse response = authService.reissueAccessToken(request);

        return GlobalResponse.ok(response);
    }

    @GetMapping("/v2/kakao")
    public ResponseEntity<GlobalResponse<AuthResponse>> kakaoLogin(@RequestParam String code) {

        AuthResponse response = authService.kakaoLogin(code);

        return GlobalResponse.ok(response);
    }

    @PatchMapping("/v2/kakao/complete")
    public ResponseEntity<GlobalResponse<Void>> completeProfile(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody KakaoProfileCompleteRequest request
    ) {
        authService.completeProfile(authUser.getUserId(), request.phone(), request.address());

        return GlobalResponse.ok(null);
    }
}
