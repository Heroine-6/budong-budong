package com.example.budongbudong.domain.payment.repository;

import com.example.budongbudong.domain.payment.dto.query.ReadAllPaymentDto;
import com.example.budongbudong.domain.payment.dto.query.ReadPaymentDetailDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.Optional;

public interface QPaymentRepository {

    Slice<ReadAllPaymentDto> findAllByUserId(Long userId, Pageable pageable);

    Optional<ReadPaymentDetailDto> findDetailById(Long paymentId);
}
