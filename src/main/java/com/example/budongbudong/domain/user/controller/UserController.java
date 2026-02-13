package com.example.budongbudong.domain.user.controller;

import com.example.budongbudong.common.dto.AuthUser;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.user.dto.response.UpdatePushAllowedResponse;
import com.example.budongbudong.domain.user.service.UserService;
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

    @PatchMapping("/v2/notifications")
    public ResponseEntity<GlobalResponse<UpdatePushAllowedResponse>> updatePushAllowed(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        UpdatePushAllowedResponse response = userService.updatePushAllowed(authUser.getUserId());

        return GlobalResponse.ok(response);
    }

    @PostMapping("/v2/kakao/link")
    public ResponseEntity<GlobalResponse<Void>> linkKakao(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam("code") String code,
            @RequestParam(value = "redirectUri", required = false) String redirectUri
    ) {
        userService.linkKakao(authUser.getUserId(), code, redirectUri);

        return GlobalResponse.ok(null);
    }
}
