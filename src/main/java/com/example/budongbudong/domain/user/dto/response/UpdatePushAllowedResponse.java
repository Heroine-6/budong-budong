package com.example.budongbudong.domain.user.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UpdatePushAllowedResponse {

    private final boolean isPushAllowed;

    public static UpdatePushAllowedResponse from(boolean isPushAllowed) {
        return new UpdatePushAllowedResponse(isPushAllowed);
    }
}
