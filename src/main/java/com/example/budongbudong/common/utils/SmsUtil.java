package com.example.budongbudong.common.utils;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class SmsUtil {

    public static String generateAuthCode() {
        Random random = new Random();
        int code = 10000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    public String makeAuthMessage(String authCode) {
        return "[부동부동 인증번호] " + authCode + "\n본인 확인을 위해 인증번호를 입력해주세요.";
    }
}
