package com.example.budongbudong.domain.payment.toss.client;

import com.example.budongbudong.domain.payment.toss.dto.response.TossConfirmResponse;
import com.example.budongbudong.domain.payment.toss.dto.response.TossPaymentStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

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

    private final TossFeignClient tossFeignClient;

    public TossConfirmResponse confirm(String paymentKey, String orderId, BigDecimal amount) {
        Map<String, Object> body = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );
        return tossFeignClient.confirm(body);
    }

    public void refund(String paymentKey, BigDecimal amount, String cancelReason) {
        Map<String, Object> body = Map.of(
                "cancelReason", cancelReason,
                "cancelAmount", amount
        );
        tossFeignClient.refund(paymentKey, body);
    }

    public TossPaymentStatusResponse getPayment(String paymentKey) {
        return tossFeignClient.getPayment(paymentKey);
    }
}
