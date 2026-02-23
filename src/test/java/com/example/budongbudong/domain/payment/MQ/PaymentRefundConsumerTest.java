package com.example.budongbudong.domain.payment.MQ;

import com.example.budongbudong.common.entity.*;
import com.example.budongbudong.domain.payment.enums.PaymentStatus;
import com.example.budongbudong.domain.payment.enums.PaymentType;
import com.example.budongbudong.domain.payment.log.service.PaymentLogService;
import com.example.budongbudong.domain.payment.repository.PaymentRepository;
import com.example.budongbudong.domain.payment.toss.client.TossPaymentClient;
import com.example.budongbudong.domain.payment.toss.exception.TossClientException;
import com.example.budongbudong.domain.payment.toss.exception.TossNetworkException;
import com.example.budongbudong.fixture.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentRefundConsumer 환불 실행 테스트")
class PaymentRefundConsumerTest {

    @InjectMocks
    private PaymentRefundConsumer paymentRefundConsumer;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentLogService paymentLogService;

    @Mock
    private TossPaymentClient tossPaymentClient;

    @Mock
    private PaymentRefundRetryPublisher refundRetryPublisher;

    User user;
    Auction auction;
    Payment payment;
    RefundRequestedMQEvent message;

    @BeforeEach
    void setUp() {
        user = UserFixture.generalUser();
        auction = AuctionFixture.openEndedAuction(
                PropertyFixture.property(user),
                LocalDateTime.now()
        );
        payment = PaymentFixture.successPayment(user,auction, PaymentType.DEPOSIT);
        ReflectionTestUtils.setField(payment, "id", 1L);

        message = new RefundRequestedMQEvent(1L);
    }

    @Nested
    @DisplayName("consume")
    class Consume {

        @Test
        @DisplayName("REFUND_REQUESTED 상태의 결제를 Toss API로 환불 후 REFUNDED로 전이된다")
        void refund_success() {
            //given
            payment.requestRefund();

            when(paymentRepository.getByIdOrThrow(1L)).thenReturn(payment);

            //when
            paymentRefundConsumer.consume(message);

            //then
            verify(tossPaymentClient).refund(
                    payment.getPaymentKey(),
                    payment.getAmount(),
                    "경매 낙찰 실패에 따른 보증금 환불"
            );
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        @DisplayName("이미 REFUNDED 상태이면 중복 환불하지 않는다 (멱등성)")
        void already_refunded_skip() {
            //given
            payment.requestRefund();
            payment.makeRefunded();

            when(paymentRepository.getByIdOrThrow(1L)).thenReturn(payment);

            //when
            paymentRefundConsumer.consume(message);

            //then
            verify(tossPaymentClient,never()).refund(any(), any(), any());
        }

        @Test
        @DisplayName("Toss API가 거절하면 로그만 남기고 상태를 유지한다")
        void toss_client_exception() {
            // given
            payment.requestRefund();

            when(paymentRepository.getByIdOrThrow(1L)).thenReturn(payment);

            doThrow(new TossClientException("거절"))
                    .when(tossPaymentClient)
                    .refund(any(), any(), any());

            // when
            paymentRefundConsumer.consume(message);

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUND_REQUESTED);
        }

        @Test
        @DisplayName("네트워크 오류 시 지연 큐에 발행하여 재시도를 유도한다")
        void toss_network_exception_retry() {
            // given
            payment.requestRefund();

            when(paymentRepository.getByIdOrThrow(1L)).thenReturn(payment);

            doThrow(new TossNetworkException("timeout"))
                    .when(tossPaymentClient)
                    .refund(any(), any(), any());

            // when
            paymentRefundConsumer.consume(message);

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUND_REQUESTED);
            assertThat(payment.getRefundRetryCount()).isEqualTo(1);
            verify(refundRetryPublisher).publish(1L);
        }
    }
}
