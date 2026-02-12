package com.example.budongbudong.domain.payment.toss.config;

import com.example.budongbudong.domain.payment.toss.exception.TossClientException;
import com.example.budongbudong.domain.payment.toss.exception.TossNetworkException;
import feign.Request;
import feign.RequestInterceptor;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class TossFeignConfig {

    @Value("${spring.toss.secret-key}")
    private String secretKey;

    @Bean
    public RequestInterceptor tossRequestInterceptor() {
        return requestTemplate -> {
            String encoded = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
            requestTemplate.header("Authorization", "Basic " + encoded);
        };
    }

    @Bean
    public ErrorDecoder tossErrorDecoder() {
        return (methodKey, response) -> {
            int status = response.status();
            String body = extractBody(response);

            if (status >= 400 && status < 500) {
                return new TossClientException(body);
            }
            return new TossNetworkException(body);
        };
    }

    @Bean
    public Request.Options tossRequestOptions() {
        return new Request.Options(3, TimeUnit.SECONDS, 5, TimeUnit.SECONDS, true);
    }

    private String extractBody(Response response) {
        try {
            if (response.body() != null) {
                return new String(response.body().asInputStream().readAllBytes());
            }
        } catch (Exception ignored) {
        }
        return "Unknown error";
    }
}
