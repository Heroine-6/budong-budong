package com.example.budongbudong.domain.notification.usernotification.controller;

import com.example.budongbudong.common.dto.AuthUser;
import com.example.budongbudong.common.response.CustomSliceResponse;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.notification.usernotification.dto.UserNotificationResponse;
import com.example.budongbudong.domain.notification.usernotification.service.UserNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "알림")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class UserNotificationController {

    private final UserNotificationService userNotificationService;

    @Operation(summary = "내 알림 목록 조회", description = "입찰·낙찰·결제 완료 등 수신된 알림을 최신순으로 조회합니다.")
    @GetMapping("/v2/my")
    public ResponseEntity<GlobalResponse<CustomSliceResponse<UserNotificationResponse>>> getMyNotifications(
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault Pageable pageable
    ) {
        CustomSliceResponse<UserNotificationResponse> slice = userNotificationService.getAllUserNotificationList(authUser.getUserId(), pageable);

        return GlobalResponse.ok(slice);
    }
}
