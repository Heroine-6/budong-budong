package com.example.budongbudong.domain.payment.toss.utils;

import com.example.budongbudong.domain.payment.toss.dto.response.TossPaymentStatusResponse;
import com.example.budongbudong.domain.payment.toss.enums.TossPaymentStatus;
import org.springframework.stereotype.Component;

/**
 * 토스 응답으로 보내주는 상태값을 도메인에서 사용하기 위한 map 클래스
 */
@Component
public class TossPaymentStatusMapper {

    public TossPaymentStatus map(TossPaymentStatusResponse response) {

        return switch(response.getStatus()) {
            case "DONE" -> TossPaymentStatus.SUCCESS;
            case "CANCELED", "FAILED" -> TossPaymentStatus.FAIL;
            default -> TossPaymentStatus.UNKNOWN;
        };
    }
}
