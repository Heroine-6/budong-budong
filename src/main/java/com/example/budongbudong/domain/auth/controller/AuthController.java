package com.example.budongbudong.domain.auth.controller;

import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.auth.service.AuthService;
import com.example.budongbudong.domain.user.dto.request.SignUpRequest;
import com.example.budongbudong.domain.user.dto.response.SignUpResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<GlobalResponse<SignUpResponse>> signUp(@Valid @RequestBody SignUpRequest request) {

        SignUpResponse response = authService.signUp(request);

        return ResponseEntity.ok(GlobalResponse.success(true, "회원가입 성공", response));
    }
}
