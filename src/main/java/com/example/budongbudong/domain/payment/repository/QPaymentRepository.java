package com.example.budongbudong.domain.payment.repository;

import com.example.budongbudong.common.entity.Payment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface QPaymentRepository {

    Slice<Payment> findAllByUserId(Long userId, Pageable pageable);
}
