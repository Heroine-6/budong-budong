package com.example.budongbudong.domain.payment.toss.client;

import com.example.budongbudong.domain.payment.toss.dto.response.TossConfirmResponse;
import com.example.budongbudong.domain.payment.toss.dto.response.TossPaymentStatusResponse;
import com.example.budongbudong.domain.payment.toss.exception.TossClientException;
import com.example.budongbudong.domain.payment.toss.exception.TossNetworkException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 토스 결제 승인 전용 클라이언트
 * - 승인 요청만 담당
 * - 실패 유형을 Exception으로만 구분
 * - 도메인 판단 로직은 포함하지 않음
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    @Value("${spring.toss.secret-key}")
    private String secretKey;

    @Value("${spring.toss.api.confirm-url}")
    private String confirmUrl;

//    @Value("${spring.toss.test.force-network-error:false}")
//    private boolean forceNetworkError;

    private final AtomicInteger verifyCount = new AtomicInteger(0);

    private final RestTemplate restTemplate = new RestTemplate();

    public TossConfirmResponse confirm(String paymentKey, String orderId, BigDecimal amount) {

//        if(forceNetworkError) {
//            throw new TossNetworkException("토스 장애");
//        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(secretKey, "");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of("paymentKey", paymentKey, "orderId", orderId, "amount", amount);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<TossConfirmResponse> response = restTemplate.postForEntity(confirmUrl, request, TossConfirmResponse.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            // 4xx 승인 불가 확정
            throw new TossClientException(e.getResponseBodyAsString());
        } catch (ResourceAccessException e) {
            // 타임아웃, 연결 실패
            throw new TossNetworkException(e);
        } catch(HttpServerErrorException e) {
            // 5xx -> 토스 장애
            throw new TossNetworkException(e);
        }
    }

    public TossPaymentStatusResponse getPayment(String paymentKey) {

//        int count = verifyCount.incrementAndGet();
//        if(count <= 2){
//            throw new TossNetworkException("토스 장애");
//        }
//        return new TossPaymentStatusResponse("DONE");

        try {
            return restTemplate.getForObject(String.format("%s/%s", confirmUrl,paymentKey), TossPaymentStatusResponse.class);
        } catch (ResourceAccessException  | HttpServerErrorException e) {
            throw new TossNetworkException(e);
        }
    }
}
