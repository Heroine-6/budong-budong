package com.example.budongbudong.fixture;

import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.domain.user.enums.UserRole;

import java.util.UUID;

public class UserFixture {

    public static User user() {
        return User.create(
                "test" + UUID.randomUUID() + "@test.com",
                "김철수",
                "password",
                "01012341234",
                "서울시 어딘가 살지롱",
                UserRole.SELLER
        );
    }
}
