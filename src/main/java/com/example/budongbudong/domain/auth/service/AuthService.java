package com.example.budongbudong.domain.auth.service;

import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.common.utils.JwtUtil;
import com.example.budongbudong.domain.auth.client.KakaoApiClient;
import com.example.budongbudong.domain.auth.client.KakaoAuthClient;
import com.example.budongbudong.domain.auth.dto.request.ReissueAccessTokenRequest;
import com.example.budongbudong.domain.auth.dto.request.SignInRequest;
import com.example.budongbudong.domain.auth.dto.request.SignUpRequest;
import com.example.budongbudong.domain.auth.dto.response.AuthResponse;
import com.example.budongbudong.domain.auth.dto.response.KakaoTokenResponse;
import com.example.budongbudong.domain.auth.dto.response.KakaoUserInfoResponse;
import com.example.budongbudong.domain.user.enums.LoginType;
import com.example.budongbudong.domain.user.enums.UserRole;
import com.example.budongbudong.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String REFRESH_TOKEN_PREFIX = "refresh-token:";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final KakaoAuthClient kakaoAuthClient;
    private final KakaoApiClient kakaoApiClient;

    @Value("${kakao.oauth.client-id}")
    private String kakaoClientId;

    @Value("${kakao.oauth.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${kakao.oauth.client-secret}")
    private String kakaoClientSecret;

    @Transactional
    public AuthResponse signUp(SignUpRequest request) {

        String verifiedKey = "SMS:VERIFIED:" + request.getPhone();
        String verified = redisTemplate.opsForValue().get(verifiedKey);

        if (!"true".equals(verified)) {
            throw new CustomException(ErrorCode.SMS_VERIFICATION_REQUIRED);
        }

        redisTemplate.delete(verifiedKey);

        userRepository.validateEmailNotExists(request.getEmail());

        User user = User.create(
                request.getEmail(),
                request.getName(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhone(),
                request.getAddress(),
                UserRole.valueOf(request.getRole())
        );

        userRepository.save(user);

        return generateAndSaveToken(user);
    }

    @Transactional
    public AuthResponse signIn(SignInRequest request) {

        String userEmail = request.getEmail();
        String rawPassword = request.getPassword();

        User user = userRepository.getActiveUserByEmailOrThrow(userEmail);

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        return generateAndSaveToken(user);
    }

    public void verifyAuthCode(String toNumber, String inputCode) {

        String redisKey = "SMS:AUTH:" + toNumber;
        String storedCode = redisTemplate.opsForValue().get(redisKey);

        if (storedCode == null) {
            throw new CustomException(ErrorCode.SMS_CODE_EXPIRED);
        }

        if (!storedCode.equals(inputCode)) {
            throw new CustomException(ErrorCode.SMS_CODE_MISMATCH);
        }

        redisTemplate.delete(redisKey);

        log.info("[AUTH] 인증번호 검증 성공 - key: {}", redisKey);

        String verifiedKey = "SMS:VERIFIED:" + toNumber;
        redisTemplate.opsForValue().set(verifiedKey, "true", 10, TimeUnit.MINUTES);
    }

    @Transactional(readOnly = true)
    public AuthResponse reissueAccessToken(ReissueAccessTokenRequest request) {

        String refreshToken = request.getRefreshToken();

        jwtUtil.validateToken(refreshToken);

        Long userId = jwtUtil.extractUserId(refreshToken);

        String refreshTokenKey = REFRESH_TOKEN_PREFIX + userId;
        String currentRefreshToken = redisTemplate.opsForValue().get(refreshTokenKey);

        if (currentRefreshToken == null || !currentRefreshToken.equals(refreshToken)) {
            redisTemplate.delete(refreshTokenKey);
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }

        User user = userRepository.getByIdOrThrow(userId);

        return generateAndSaveToken(user);
    }

    @Transactional
    public AuthResponse kakaoLogin(String code) {
        KakaoTokenResponse tokenResponse = kakaoAuthClient.getToken(
                "authorization_code", kakaoClientId, kakaoRedirectUri, code, kakaoClientSecret
        );

        KakaoUserInfoResponse userInfo = kakaoApiClient.getUserInfo(
                "Bearer " + tokenResponse.accessToken()
        );

        String kakaoId = String.valueOf(userInfo.id());
        String email = userInfo.getEmail();

        if (email == null || email.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        User user = userRepository.findByLoginTypeAndProviderId(LoginType.KAKAO, kakaoId)
                .orElseGet(() -> {
                    User newUser = User.createKakaoUser(email, kakaoId);
                    return userRepository.save(newUser);
                });

        return generateAndSaveToken(user, user.isProfileComplete());
    }

    @Transactional
    public void completeProfile(Long userId, String name, String phone, String address) {
        User user = userRepository.getByIdOrThrow(userId);
        user.completeProfile(name, phone, address);
    }

    private AuthResponse generateAndSaveToken(User user) {
        return generateAndSaveToken(user, true);
    }

    private AuthResponse generateAndSaveToken(User user, boolean isProfileComplete) {

        String refreshTokenKey = REFRESH_TOKEN_PREFIX + user.getId();

        String accessToken = jwtUtil.generateAccessToken(user.getName(), user.getEmail(), user.getRole().name(), user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        redisTemplate.opsForValue().set(refreshTokenKey, refreshToken.substring(7), 14, TimeUnit.DAYS);

        return new AuthResponse(accessToken, refreshToken, isProfileComplete);
    }

    @Transactional(readOnly = true)
    public void verifyEmail(String email) {
        
        userRepository.validateEmailNotExists(email);

    }
}

