package com.example.budongbudong.domain.notification.usernotification.controller;

import com.example.budongbudong.common.dto.AuthUser;
import com.example.budongbudong.common.response.CustomSliceResponse;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.notification.usernotification.dto.UserNotificationResponse;
import com.example.budongbudong.domain.notification.usernotification.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications/v2")
@RequiredArgsConstructor
public class UserNotificationController {

    private final UserNotificationService userNotificationService;

    @GetMapping("/my")
    public ResponseEntity<GlobalResponse<CustomSliceResponse<UserNotificationResponse>>> getMyNotifications(
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault Pageable pageable
    ) {
        CustomSliceResponse<UserNotificationResponse> slice = userNotificationService.getAllUserNotificationList(authUser.getUserId(), pageable);

        return GlobalResponse.ok(slice);
    }
}
