package com.example.budongbudong.domain.user.controller;

import com.example.budongbudong.common.dto.AuthUser;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.user.dto.response.UpdatePushAllowedResponse;
import com.example.budongbudong.domain.user.dto.response.UserInfoResponse;
import com.example.budongbudong.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "푸시 알림 수신 동의 토글", description = "푸시 알림 수신 여부를 ON/OFF 전환합니다.")
    @PatchMapping("/v2/notifications")
    public ResponseEntity<GlobalResponse<UpdatePushAllowedResponse>> updatePushAllowed(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        UpdatePushAllowedResponse response = userService.updatePushAllowed(authUser.getUserId());

        return GlobalResponse.ok(response);
    }

    @Operation(summary = "카카오 계정 연동", description = "기존 일반 계정에 카카오 계정을 연동합니다. 카카오 인가 코드가 필요합니다.")
    @PostMapping("/v2/kakao/link")
    public ResponseEntity<GlobalResponse<Void>> linkKakao(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam("code") String code,
            @RequestParam(value = "redirectUri", required = false) String redirectUri
    ) {
        userService.linkKakao(authUser.getUserId(), code, redirectUri);

        return GlobalResponse.ok(null);
    }

    @Operation(summary = "내 프로필 조회", description = "로그인한 사용자의 이름, 전화번호, 주소를 조회합니다.")
    @GetMapping("/v2/me")
    public ResponseEntity<GlobalResponse<UserInfoResponse>> getUserInfo(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        UserInfoResponse response = userService.getUserInfo(authUser.getUserId());

        return GlobalResponse.ok(response);
    }
}
