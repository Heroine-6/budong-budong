package com.example.budongbudong.domain.payment.log.service;

import com.example.budongbudong.domain.payment.enums.PaymentStatus;
import com.example.budongbudong.domain.payment.log.entity.PaymentLog;
import com.example.budongbudong.domain.payment.log.enums.LogType;
import com.example.budongbudong.domain.payment.log.repository.PaymentLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentLogService {

    private final PaymentLogRepository paymentLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(Long paymentId, PaymentStatus prev, PaymentStatus current, LogType type, String errorMessage) {
        try {
            paymentLogRepository.save(PaymentLog.create(paymentId, prev, current, type, errorMessage));
        } catch (Exception e) {
            log.warn("[결제 로그 실패] paymentId={}, type={}, error={}", paymentId, type, e.getMessage());
        }
    }
}
