package com.example.budongbudong.domain.user.entity;

import com.example.budongbudong.common.entity.BaseEntity;
import com.example.budongbudong.domain.user.enums.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.GENERAL;

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
}
