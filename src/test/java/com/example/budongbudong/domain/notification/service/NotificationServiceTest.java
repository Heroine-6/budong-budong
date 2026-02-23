package com.example.budongbudong.domain.notification.service;

import com.example.budongbudong.common.entity.Notification;
import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.common.entity.UserNotification;
import com.example.budongbudong.domain.auth.service.KakaoTokenService;
import com.example.budongbudong.domain.notification.client.KakaoClient;
import com.example.budongbudong.domain.notification.dto.response.KakaoNotificationResponse;
import com.example.budongbudong.domain.notification.enums.NotificationType;
import com.example.budongbudong.domain.notification.usernotification.repository.UserNotificationRepository;
import com.example.budongbudong.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    void sendMessage_linkedUser_success() throws Exception {
        // given
        Long userNotificationId = 1L;
        Long userId = 1L;
        String accessToken = "test_access_token";

        User user = User.create("test@test.com", "테스트", "password", "01012345678", "서울시", UserRole.GENERAL);
        setField(user, "id", userId);

        UserNotification userNotification = UserNotification.create(mock(Notification.class), user);

        given(userNotificationRepository.getByIdOrThrow(userNotificationId)).willReturn(userNotification);
        given(kakaoTokenService.getAccessToken(userId)).willReturn(accessToken);
        given(kakaoClient.sendToMeMessage(anyString(), anyString())).willReturn(mock(KakaoNotificationResponse.class));

        // when
        notificationService.sendMessage(userNotificationId, "알림 내용");

        // then
        verify(kakaoClient).sendToMeMessage(eq("Bearer " + accessToken), any());
        verify(userNotificationRepository).save(any(UserNotification.class));
    }

    @Test
    @DisplayName("카카오 미연동 유저 알림 발송 건너뛰기")
    void sendMessage_unlinkedUser_shouldSkip() throws Exception {
        // given
        Long userNotificationId = 1L;
        Long userId = 1L;

        User user = User.create("test@test.com", "테스트", "password", "01012345678", "서울시", UserRole.GENERAL);
        setField(user, "id", userId);

        UserNotification userNotification = UserNotification.create(mock(Notification.class), user);

        given(userNotificationRepository.getByIdOrThrow(userNotificationId)).willReturn(userNotification);
        given(kakaoTokenService.getAccessToken(userId)).willReturn(null);

        // when
        notificationService.sendMessage(userNotificationId, "알림 내용");

        // then
        verify(kakaoClient, never()).sendToMeMessage(anyString(), any());
        verify(userNotificationRepository, never()).save(any(UserNotification.class));
    }

    private void setField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}
