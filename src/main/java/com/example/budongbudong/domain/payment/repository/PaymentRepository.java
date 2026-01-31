package com.example.budongbudong.domain.payment.repository;

import com.example.budongbudong.common.entity.Payment;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.domain.payment.enums.PaymentFailureReason;
import com.example.budongbudong.domain.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("""
           select coalesce(sum(p.amount), 0)
           from Payment p
           where p.auction.id = :auctionId
           and p.status = 'SUCCESS'
           """)
    BigDecimal sumPaidAmountByAuctionId(Long id);

    Optional<Payment> findByOrderId(String orderId);

    List<Payment> findAllByStatusAndFailureReasonIn(PaymentStatus status, List<PaymentFailureReason> failureReasons);

    Optional<Payment> findByPaymentKey(String paymentKey);


    default Payment getByOrderIdOrThrow(String orderId) {
        return findByOrderId(orderId).orElseThrow(()-> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    default Payment getByPaymentKeyOrThrow(String paymentKey) {
        return findByPaymentKey(paymentKey).orElseThrow(()-> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    default Payment getByIdOrThrow(Long paymentId) {
        return findById(paymentId).orElseThrow(()-> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
    }

}
