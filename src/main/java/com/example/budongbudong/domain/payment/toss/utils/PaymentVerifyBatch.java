package com.example.budongbudong.domain.payment.toss.utils;

import com.example.budongbudong.common.entity.Payment;
import com.example.budongbudong.domain.payment.enums.PaymentStatus;
import com.example.budongbudong.domain.payment.repository.PaymentRepository;
import com.example.budongbudong.domain.payment.toss.client.TossPaymentClient;
import com.example.budongbudong.domain.payment.toss.dto.response.TossPaymentStatusResponse;
import com.example.budongbudong.domain.payment.toss.enums.TossPaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * VERIFYING 상태의 결제를 재확인 하는 배치 클래스
 * - PG 결제 상태 조회 후 최종 상태 확인
 */
@Component
@RequiredArgsConstructor
public class PaymentVerifyBatch {

    private final TossPaymentStatusMapper mapper;
    private final TossPaymentClient client;
    private final PaymentRepository paymentRepository;

    @Transactional
    public void verify() {

        List<Payment> paymentList = paymentRepository.findAllByStatus(PaymentStatus.VERIFYING);

        for (Payment payment : paymentList) {
            TossPaymentStatusResponse response = client.getPayment(payment.getPaymentKey());
            TossPaymentStatus tossStatus = mapper.map(response);
            payment.finalizeByTossStatus(tossStatus, null, null);
        }
    }
}
