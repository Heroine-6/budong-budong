package com.example.budongbudong.domain.auth.service;

import com.example.budongbudong.domain.auth.client.KakaoAuthClient;
import com.example.budongbudong.domain.auth.dto.response.KakaoTokenResponse;
import com.example.budongbudong.domain.notification.client.KakaoClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoTokenService {

    private static final String KAKAO_ACCESS_PREFIX = "kakao:access:";
    private static final String KAKAO_REFRESH_PREFIX = "kakao:refresh:";
    private static final long ACCESS_TOKEN_TTL_HOURS = 5;
    private static final long REFRESH_TOKEN_TTL_DAYS = 58;

    private final RedisTemplate<String, String> redisTemplate;
    private final KakaoAuthClient kakaoAuthClient;
    private final KakaoClient kakaoClient;

    @Value("${kakao.oauth.client-id}")
    private String clientId;

    @Value("${kakao.oauth.client-secret}")
    private String clientSecret;

    @Value("${kakao.oauth.redirect-uri}")
    private String redirectUri;

    /**
     * 인가 코드로 카카오 토큰 발급
     */
    public KakaoTokenResponse issueToken(String authorizationCode, String callbackUri) {
        return kakaoAuthClient.getToken(
                "authorization_code",
                clientId,
                callbackUri != null ? callbackUri : redirectUri,
                authorizationCode,
                clientSecret
        );
    }

    /**
     * 카카오 토큰을 Redis에 저장
     */
    public void saveTokens(Long userId, String accessToken, String refreshToken) {
        redisTemplate.opsForValue().set(
                KAKAO_ACCESS_PREFIX + userId,
                accessToken,
                ACCESS_TOKEN_TTL_HOURS,
                TimeUnit.HOURS
        );

        if (refreshToken != null) {
            redisTemplate.opsForValue().set(
                    KAKAO_REFRESH_PREFIX + userId,
                    refreshToken,
                    REFRESH_TOKEN_TTL_DAYS,
                    TimeUnit.DAYS
            );
        }
    }

    /**
     * 유저의 카카오 access_token 조회 (만료 시 자동 갱신)
     */
    public String getAccessToken(Long userId) {
        String accessToken = redisTemplate.opsForValue().get(KAKAO_ACCESS_PREFIX + userId);

        if (accessToken != null) {
            return accessToken;
        }

        return refreshAccessToken(userId);
    }

    /**
     * 카카오 사용자 ID 조회
     */
    public String getKakaoUserId(String accessToken) {
        Map<String, Object> userInfo = kakaoClient.getUserInfo("Bearer " + accessToken);
        return String.valueOf(userInfo.get("id"));
    }

    /**
     * refresh_token으로 access_token 재발급
     */
    private String refreshAccessToken(Long userId) {
        String refreshToken = redisTemplate.opsForValue().get(KAKAO_REFRESH_PREFIX + userId);

        if (refreshToken == null) {
            log.debug("[카카오 토큰] 미연동 유저 - userId={}", userId);
            return null;
        }

        try {
            KakaoTokenResponse response = kakaoAuthClient.refreshToken(
                    "refresh_token",
                    clientId,
                    refreshToken,
                    clientSecret
            );

            saveTokens(userId, response.getAccessToken(), response.getRefreshToken());

            log.info("[카카오 토큰] 갱신 성공 - userId={}", userId);
            return response.getAccessToken();

        } catch (Exception e) {
            log.error("[카카오 토큰] 갱신 실패 - userId={}, error={}", userId, e.getMessage());
            return null;
        }
    }
}
