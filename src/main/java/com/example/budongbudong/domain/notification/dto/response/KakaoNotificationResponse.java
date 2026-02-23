package com.example.budongbudong.domain.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoNotificationResponse {

    @JsonProperty("result_code")
    private int resultCode;

    @JsonProperty("result_msg")
    private String resultMsg;
}