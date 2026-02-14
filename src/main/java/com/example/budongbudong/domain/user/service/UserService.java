package com.example.budongbudong.domain.user.service;

import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.domain.auth.dto.response.KakaoTokenResponse;
import com.example.budongbudong.domain.auth.service.KakaoTokenService;
import com.example.budongbudong.domain.user.dto.response.UpdatePushAllowedResponse;
import com.example.budongbudong.domain.user.dto.response.UserInfoResponse;
import com.example.budongbudong.domain.user.enums.LoginType;
import com.example.budongbudong.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final KakaoTokenService kakaoTokenService;

    /**
     * 알림 수신 동의 변경
     */
    @Transactional
    public UpdatePushAllowedResponse updatePushAllowed(Long userId) {

        User user = userRepository.getByIdOrThrow(userId);
        user.updatePushAllowed();

        return UpdatePushAllowedResponse.from(user.isPushAllowed());
    }

    /**
     * 카카오 계정 연동
     */
    @Transactional
    public void linkKakao(Long userId, String authorizationCode, String redirectUri) {
        KakaoTokenResponse tokenResponse = kakaoTokenService.issueToken(authorizationCode, redirectUri);

        kakaoTokenService.saveTokens(userId, tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());

        String kakaoId = kakaoTokenService.getKakaoUserId(tokenResponse.getAccessToken());

        User user = userRepository.getByIdOrThrow(userId);
        user.linkKakao(LoginType.KAKAO, kakaoId);
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(Long userId) {

        User user = userRepository.getByIdOrThrow(userId);

        return UserInfoResponse.from(user);
    }
}
