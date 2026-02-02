package com.example.budongbudong.domain.payment.repository;

import com.example.budongbudong.domain.payment.dto.ReadAllPaymentDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface QPaymentRepository {

    Slice<ReadAllPaymentDto> findAllByUserId(Long userId, Pageable pageable);
}
