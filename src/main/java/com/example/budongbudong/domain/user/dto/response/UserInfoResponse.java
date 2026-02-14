package com.example.budongbudong.domain.user.dto.response;

import com.example.budongbudong.common.entity.User;

public record UserInfoResponse(String name, String phone, String address) {
    public static UserInfoResponse from(User user) {
        return new UserInfoResponse(user.getName(), user.getPhone(), user.getAddress());
    }
}
