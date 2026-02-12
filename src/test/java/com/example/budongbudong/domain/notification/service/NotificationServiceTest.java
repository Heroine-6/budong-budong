package com.example.budongbudong.domain.notification.service;

import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.common.entity.UserNotification;
import com.example.budongbudong.domain.auth.service.KakaoTokenService;
import com.example.budongbudong.domain.notification.client.KakaoClient;
import com.example.budongbudong.domain.notification.repository.UserNotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private UserNotificationRepository userNotificationRepository;

    @Mock
    private KakaoTokenService kakaoTokenService;

    @Mock
    private KakaoClient kakaoClient;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("카카오 연동 유저 알림 발송 성공")
    void sendNotification_linkedUser_success() {
        // given
        Long userNotificationId = 1L;
        Long userId = 1L;
        String accessToken = "test_access_token";

        User user = User.builder().id(userId).build();
        UserNotification userNotification = UserNotification.builder()
                .user(user)
                .sendAt(null) // 발송되지 않은 알림
                .build();

        given(userNotificationRepository.getByIdOrThrow(userNotificationId)).willReturn(userNotification);
        given(kakaoTokenService.getAccessToken(userId)).willReturn(accessToken);

        // when
        notificationService.updateSendAtAndNotifiedAt(userNotificationId, "알림 내용", LocalDateTime.now());

        // then
        verify(kakaoClient).sendToMeMessage(eq("Bearer " + accessToken), any());
        verify(userNotificationRepository).save(any(UserNotification.class));
    }

    @Test
    @DisplayName("카카오 미연동 유저 알림 발송 건너뛰기")
    void sendNotification_unlinkedUser_shouldSkip() {
        // given
        Long userNotificationId = 1L;
        Long userId = 1L;

        User user = User.builder().id(userId).build();
        UserNotification userNotification = UserNotification.builder()
                .user(user)
                .sendAt(null) // 발송되지 않은 알림
                .build();

        given(userNotificationRepository.getByIdOrThrow(userNotificationId)).willReturn(userNotification);
        given(kakaoTokenService.getAccessToken(userId)).willReturn(null); // 토큰 없음

        // when
        notificationService.updateSendAtAndNotifiedAt(userNotificationId, "알림 내용", LocalDateTime.now());

        // then
        verify(kakaoClient, never()).sendToMeMessage(anyString(), any());
        verify(userNotificationRepository, never()).save(any(UserNotification.class));
    }
}
