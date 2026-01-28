package com.example.budongbudong.domain.payment.repository;

import com.example.budongbudong.common.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
