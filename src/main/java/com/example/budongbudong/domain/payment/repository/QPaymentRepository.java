package com.example.budongbudong.domain.payment.repository;

import com.example.budongbudong.domain.payment.dto.query.*;
import com.example.budongbudong.domain.payment.enums.PaymentType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Optional;

public interface QPaymentRepository {

    Slice<ReadAllPaymentDto> findAllByUserId(Long userId, Pageable pageable);

    Optional<ReadPaymentDetailDto> findDetailById(Long paymentId);

    List<Long> findDepositPaymentIdsByAuctionIdAndNotWinnerUserId(Long auctionId, Long winnerUserId);

    List<RequiredPaymentDto> findRequiredPaymentsByUserId(Long userId, PaymentType type);
}
