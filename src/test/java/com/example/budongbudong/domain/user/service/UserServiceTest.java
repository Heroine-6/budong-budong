package com.example.budongbudong.domain.user.service;

import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.domain.auth.dto.response.KakaoTokenResponse;
import com.example.budongbudong.domain.auth.service.KakaoTokenService;
import com.example.budongbudong.domain.user.enums.LoginType;
import com.example.budongbudong.domain.user.enums.UserRole;
import com.example.budongbudong.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KakaoTokenService kakaoTokenService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("카카오 계정 연동 성공")
    void linkKakao_success() throws Exception {
        // given
        Long userId = 1L;
        String authorizationCode = "test_auth_code";
        String accessToken = "test_access_token";
        String refreshToken = "test_refresh_token";

        User user = User.create("test@test.com", "테스트", "password", "01012345678", "서울시", UserRole.GENERAL);
        KakaoTokenResponse tokenResponse = createKakaoTokenResponse(accessToken, refreshToken);

        String redirectUri = "http://localhost:5173/mypage";
        String kakaoId = "12345";

        given(kakaoTokenService.issueToken(authorizationCode, redirectUri)).willReturn(tokenResponse);
        given(kakaoTokenService.getKakaoUserId(accessToken)).willReturn(kakaoId);
        given(userRepository.getByIdOrThrow(userId)).willReturn(user);

        // when
        userService.linkKakao(userId, authorizationCode, redirectUri);

        // then
        verify(kakaoTokenService).saveTokens(userId, accessToken, refreshToken);
        assertThat(user.getLoginType()).isEqualTo(LoginType.KAKAO);
        assertThat(user.getProviderId()).isEqualTo(kakaoId);
    }

    private KakaoTokenResponse createKakaoTokenResponse(String accessToken, String refreshToken) throws Exception {
        KakaoTokenResponse response = new KakaoTokenResponse();
        setField(response, "accessToken", accessToken);
        setField(response, "refreshToken", refreshToken);
        return response;
    }

    private void setField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}
