package com.example.budongbudong.domain.payment.log.repository;

import com.example.budongbudong.domain.payment.log.entity.PaymentLog;
import com.example.budongbudong.domain.payment.log.enums.LogType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {

    List<PaymentLog> findByPaymentId(Long paymentId);

    List<PaymentLog> findByPaymentIdAndLogType(Long paymentId, LogType logType);
}
