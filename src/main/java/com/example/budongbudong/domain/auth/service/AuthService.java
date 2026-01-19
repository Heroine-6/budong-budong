package com.example.budongbudong.domain.auth.service;

import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.common.utils.JwtUtil;
import com.example.budongbudong.domain.auth.dto.request.SignInRequest;
import com.example.budongbudong.domain.auth.dto.request.SignUpRequest;
import com.example.budongbudong.domain.auth.dto.response.AuthResponse;
import com.example.budongbudong.domain.user.enums.UserRole;
import com.example.budongbudong.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public AuthResponse signUp(SignUpRequest request) {

        String verifiedKey = "SMS:VERIFIED:" + request.getPhone();
        String verified = redisTemplate.opsForValue().get(verifiedKey);

        if (!"true".equals(verified)) {
            throw new CustomException(ErrorCode.SMS_VERIFICATION_REQUIRED);
        }

        redisTemplate.delete(verifiedKey);

        String userEmail = request.getEmail();

        userRepository.validateEmailNotExists(userEmail);

        User user = User.create(
                userEmail,
                request.getName(),
                passwordEncoder.encode(request.getPassword()),
                request.getPhone(),
                request.getAddress(),
                UserRole.valueOf(request.getRole())
        );

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getName(), userEmail, user.getRole().name(), user.getId());

        return new AuthResponse(token);
    }

    @Transactional
    public AuthResponse signIn(SignInRequest request) {

        String userEmail = request.getEmail();
        String rawPassword = request.getPassword();

        User user = userRepository.getActiveUserByEmailOrThrow(userEmail);

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        String token = jwtUtil.generateToken(user.getName(), user.getEmail(), user.getRole().name(), user.getId());

        return new AuthResponse(token);
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
}

