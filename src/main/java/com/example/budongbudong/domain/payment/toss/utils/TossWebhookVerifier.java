package com.example.budongbudong.domain.payment.toss.utils;

import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;

/**
 * 서명 검증 유틸 클래스
 */
@Component
public class TossWebhookVerifier {

    @Value("${spring.toss.secret-key}")
    private String secretKey;

    private  static final String hmac = "HmacSHA256";

    public void verify(Map<String, String> headers, String rawBody) {
        String signature = headers.get("x-toss-signature");

        String expected = hmacSha256(secretKey, rawBody);

        if (!expected.equals(signature)) {
            throw new SecurityException(ErrorCode.INVALID_WEBHOOK_SIGNATURE.getMessage());
        }
    }

    private String hmacSha256(String secret, String body) {
        try{
            Mac mac = Mac.getInstance(hmac);

            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), hmac);
            mac.init(keySpec);

            byte[] rawHmac = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(rawHmac);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(ErrorCode.FAIL_HMAC.getMessage(), e);
        }

    }
}
