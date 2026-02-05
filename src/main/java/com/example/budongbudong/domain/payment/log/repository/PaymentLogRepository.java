package com.example.budongbudong.domain.payment.log.repository;

import com.example.budongbudong.domain.payment.log.entity.PaymentLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentLogRepository extends JpaRepository<PaymentLog, Long> {
}
