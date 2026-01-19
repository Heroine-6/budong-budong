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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse signUp(SignUpRequest request) {

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
}

