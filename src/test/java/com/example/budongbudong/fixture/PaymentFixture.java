package com.example.budongbudong.fixture;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.Payment;
import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.domain.payment.toss.enums.PaymentFailureReason;
import com.example.budongbudong.domain.payment.enums.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentFixture {

    private static final BigDecimal AMOUNT = new BigDecimal("100000");
    private static final String ORDER_NAME = "테스트 자이 아파트 101동 101호";
    private static final String ORDER_ID = "ORDER-001";
    private static final String PAYMENT_KEY = "paymentKey-test-123";

    public static Payment payment(User user, Auction auction, PaymentType type) {
        return Payment.builder()
                .user(user)
                .auction(auction)
                .type(type)
                .orderName(ORDER_NAME)
                .amount(AMOUNT)
                .orderId(ORDER_ID)
                .build();
    }

    public static Payment successPayment(User user, Auction auction, PaymentType type) {
        Payment payment = payment(user, auction, type);
        payment.makeSuccess(PAYMENT_KEY, LocalDateTime.now(), null, null);
        return payment;
    }

    public static Payment verifyingPayment(User user, Auction auction, PaymentType type) {
        Payment payment = payment(user, auction, type);
        payment.makeVerifying(PaymentFailureReason.PG_TIMEOUT, PAYMENT_KEY);
        return payment;
    }

    public static Payment inprogressPayment(User user, Auction auction, PaymentType type) {
        Payment payment = payment(user, auction, type);
        payment.makeInProgress(PAYMENT_KEY);
        return payment;
    }
}
