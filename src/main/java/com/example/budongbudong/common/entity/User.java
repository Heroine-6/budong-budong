package com.example.budongbudong.common.entity;

import com.example.budongbudong.domain.user.enums.LoginType;
import com.example.budongbudong.domain.user.enums.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "password")
    private String password;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.GENERAL;

    @Column(name = "login_type")
    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    @Column(name = "provider_id", unique = true)
    private String providerId;

    @Column(name = "is_push_allowed", nullable = false)
    private boolean isPushAllowed = true;

    public static User create(
            String email,
            String name,
            String password,
            String phone,
            String address,
            UserRole role
    ) {
        User user = new User();
        user.email = email;
        user.name = name;
        user.password = password;
        user.phone = phone;
        user.address = address;
        user.role = role;
        return user;
    }

    public static User createKakaoUser(String email, String kakaoId) {
        User user = new User();
        user.email = email;
        user.name = "카카오유저";
        user.password = UUID.randomUUID().toString();
        user.loginType = LoginType.KAKAO;
        user.providerId = kakaoId;
        user.role = UserRole.GENERAL;
        return user;
    }

    public void completeProfile(String phone, String address) {
        if (phone == null || phone.isBlank() || address == null || address.isBlank()) {
            throw new IllegalArgumentException("전화번호와 주소는 필수입니다.");
        }
        this.phone = phone;
        this.address = address;
    }

    public boolean isProfileComplete() {
        return phone != null && !phone.isBlank()
                && address != null && !address.isBlank();
    }

    public boolean isKakaoUser() {
        return loginType == LoginType.KAKAO;
    }

    public void updatePushAllowed() {
        this.isPushAllowed = !this.isPushAllowed;
    }

    public void linkKakao(LoginType loginType, String providerId) {
        this.loginType = loginType;
        this.providerId = providerId;
    }
}
