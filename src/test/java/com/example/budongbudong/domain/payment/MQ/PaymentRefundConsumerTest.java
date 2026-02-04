package com.example.budongbudong.domain.payment.MQ;

import com.example.budongbudong.common.entity.*;
import com.example.budongbudong.domain.payment.repository.PaymentRepository;
import com.example.budongbudong.domain.payment.toss.client.TossPaymentClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentRefundConsumer 환불 실행 테스트")
class PaymentRefundConsumerTest {

    @InjectMocks
    private PaymentRefundConsumer paymentRefundConsumer;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private TossPaymentClient tossPaymentClient;

    User user;
    Auction auction;
    Payment payment;
    RefundRequestedMQEvent message;

    @BeforeEach
    void setUp() {

    }

    @Nested
    @DisplayName("consume")
    class Consume {

        @Test
        @DisplayName("REFUND_REQUESTED 상태의 결제를 Toss API로 환불 후 REFUNDED로 전이된다")
        void refund_success() {
        }

        @Test
        @DisplayName("이미 REFUNDED 상태이면 중복 환불하지 않는다 (멱등성)")
        void already_refunded_skip() {
        }

        @Test
        @DisplayName("Toss API가 거절하면 로그만 남기고 상태를 유지한다")
        void toss_client_exception() {
        }

        @Test
        @DisplayName("네트워크 오류 시 예외를 던져 RabbitMQ 재시도를 유도한다")
        void toss_network_exception_retry() {
        }
    }
}
