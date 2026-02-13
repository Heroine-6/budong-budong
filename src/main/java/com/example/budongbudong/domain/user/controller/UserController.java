package com.example.budongbudong.domain.user.controller;

import com.example.budongbudong.common.dto.AuthUser;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.user.dto.response.UpdatePushAllowedResponse;
import com.example.budongbudong.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/v2")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/notifications")
    public ResponseEntity<GlobalResponse<UpdatePushAllowedResponse>> updatePushAllowed(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        UpdatePushAllowedResponse response = userService.updatePushAllowed(authUser.getUserId());

        return GlobalResponse.ok(response);
    }

    @PostMapping("/kakao/link")
    public ResponseEntity<GlobalResponse<Void>> linkKakao(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam("code") String code,
            @RequestParam(value = "redirectUri", required = false) String redirectUri
    ) {
        userService.linkKakao(authUser.getUserId(), code, redirectUri);

        return GlobalResponse.ok(null);
    }
}
