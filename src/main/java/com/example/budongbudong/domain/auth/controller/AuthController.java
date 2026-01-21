package com.example.budongbudong.domain.auth.controller;

import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.auth.dto.request.*;
import com.example.budongbudong.domain.auth.dto.response.AuthResponse;
import com.example.budongbudong.domain.auth.service.AuthService;
import com.example.budongbudong.domain.auth.service.SmsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SmsService smsService;

    @PostMapping("/send")
    public ResponseEntity<GlobalResponse<Void>> sendAuthCode(@RequestBody SmsSendRequest request) {

        smsService.sendAuthCode(request.toNumber());

        return GlobalResponse.ok(null);
    }

    @PostMapping("/verify")
    public ResponseEntity<GlobalResponse<Void>> verifyAuthCode(@RequestBody SmsVerifyRequest request) {

        authService.verifyAuthCode(request.toNumber(), request.code());

        return GlobalResponse.ok(null);
    }

    @PostMapping("/signup")
    public ResponseEntity<GlobalResponse<AuthResponse>> signUp(@Valid @RequestBody SignUpRequest request) {

        AuthResponse response = authService.signUp(request);

        return GlobalResponse.ok(response);
    }

    @PostMapping("/signin")
    public ResponseEntity<GlobalResponse<AuthResponse>> signIn(@Valid @RequestBody SignInRequest request) {

        AuthResponse response = authService.signIn(request);

        return GlobalResponse.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<GlobalResponse<AuthResponse>> reissueAccessToken(@Valid @RequestBody ReissueAccessTokenRequest request) {

        AuthResponse response = authService.reissueAccessToken(request);

        return GlobalResponse.ok(response);
    }
}
