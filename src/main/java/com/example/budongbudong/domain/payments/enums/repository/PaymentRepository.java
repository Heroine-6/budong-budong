package com.example.budongbudong.domain.payments.enums.repository;

import com.example.budongbudong.common.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
