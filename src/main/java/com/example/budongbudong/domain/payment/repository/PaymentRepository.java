package com.example.budongbudong.domain.payment.repository;

import com.example.budongbudong.common.entity.*;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.domain.payment.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long>, QPaymentRepository {

    @Query("""
           select coalesce(sum(p.amount), 0)
           from Payment p
           where p.auction.id = :auctionId
           and p.status = 'SUCCESS'
           and p.type in :types
           """)
    BigDecimal sumPaidAmountByAuctionId(Long auctionId, List<PaymentType> types);

    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findByAuctionAndTypeAndStatus(Auction auction, PaymentType paymentType, PaymentStatus paymentStatus);

    List<Payment> findAllByStatus(PaymentStatus paymentStatus);

    Optional<Payment> findByIdAndUserId(Long id, Long userId);

    boolean existsByAuctionIdAndTypeAndStatus(Long id, PaymentType paymentType, PaymentStatus paymentStatus);

    @Query("""
            select p
            from Payment p
            where p.auction.id = :auctionId
              and p.type = 'DOWN_PAYMENT'
              and p.status = 'SUCCESS'
        """)
    Optional<Payment> findSuccessDownPayment(Long auctionId);

    boolean existsByUserAndAuctionAndTypeAndStatus(User user, Auction auction, PaymentType type, PaymentStatus status);

    default Payment getByOrderIdOrThrow(String orderId) {
        return findByOrderId(orderId).orElseThrow(()-> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    default Payment getByIdOrThrow(Long paymentId) {
        return findById(paymentId).orElseThrow(()-> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    default Payment getByIdAndUserIdOrThrow(Long id, Long userId) {
        return findByIdAndUserId(id,userId).orElseThrow(()-> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    default Payment getSuccessDownPayment(Long auctionId) {
        return findSuccessDownPayment(auctionId).orElseThrow(() -> new CustomException(ErrorCode.DEPOSIT_REQUIRED_FIRST));
    }

}
