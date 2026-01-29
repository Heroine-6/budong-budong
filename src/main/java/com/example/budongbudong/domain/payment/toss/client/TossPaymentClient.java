package com.example.budongbudong.domain.payment.toss.client;

import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    @Value("${spring.toss.secret-key}")
    private String secretKey;

    @Value("${spring.toss.api.confirm-url}")
    private String confirmUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void confirm(String paymentKey, String orderId, BigDecimal amount) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(secretKey, "");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of("paymentKey", paymentKey, "orderId", orderId, "amount", amount);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(confirmUrl, request, String.class);
        } catch (HttpClientErrorException e) {
            log.error("Toss confirm error status = {}", e.getStatusCode());
            log.error("Toss confirm error body = {}", e.getResponseBodyAsString());
            throw new CustomException(ErrorCode.PAYMENT_FAILURE);
        }
    }
}
