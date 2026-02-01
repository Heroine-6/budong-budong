package com.example.budongbudong.domain.payment.dto.response;

import com.example.budongbudong.domain.payment.enums.PaymentStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 결제 요청 생성 응답 DTO
 * - 프론트가 결제 창 오픈을 할 수 있게 정보제공 해주는 dto
 */
@Getter
@RequiredArgsConstructor
public class PaymentReadyResponse {
    private final Long paymentId;
    private final PaymentStatus status;
    private final PaymentTossReadyResponse toss;
}
