package com.example.budongbudong.domain.notification.client;

import com.example.budongbudong.domain.notification.dto.KakaoNotificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import static org.apache.http.HttpHeaders.AUTHORIZATION;

@FeignClient(
        name = "kakaoClient",
        url = "https://kapi.kakao.com"
)
public interface KakaoClient {

    @PostMapping(
            value = "/v2/api/talk/memo/default/send",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    KakaoNotificationResponse sendToMeMessage(
            @RequestHeader(name = AUTHORIZATION) String accessToken,
            @RequestBody String templateObjectJson
    );

}