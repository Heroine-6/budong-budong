package com.example.budongbudong.fixture;

import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.domain.user.enums.UserRole;

import java.util.UUID;

public class UserFixture {

    public static User sellerUser() {
        return User.create(
                "test" + UUID.randomUUID() + "@test.com",
                "김철수",
                "password",
                "01012341234",
                "서울시 어딘가 살지롱",
                UserRole.SELLER
        );
    }

    public static User generalUser() {
        return User.create(
                "test1" + UUID.randomUUID() + "@test.com",
                "김맹구",
                "password",
                "01012341234",
                "서울시 어딘가 살지롱",
                UserRole.GENERAL
        );
    }

    public static User user2() {
        return User.create(
                "test2" + UUID.randomUUID() + "@test.com",
                "김짱구",
                "password",
                "01012341234",
                "부산시 어딘가 살지롱",
                UserRole.ADMIN
        );
    }
}
