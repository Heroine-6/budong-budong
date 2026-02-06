package com.example.budongbudong.domain.payment.MQ;

import com.example.budongbudong.common.entity.*;
import com.example.budongbudong.domain.payment.enums.PaymentStatus;
import com.example.budongbudong.domain.payment.enums.PaymentType;
import com.example.budongbudong.domain.payment.log.service.PaymentLogService;
import com.example.budongbudong.domain.payment.repository.PaymentRepository;
import com.example.budongbudong.domain.payment.toss.client.TossPaymentClient;
import com.example.budongbudong.domain.payment.toss.exception.TossNetworkException;
import com.example.budongbudong.fixture.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * defaultRequeueRejected=true 설정 시 무한 재시도 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("무한 재시도 시뮬레이션 테스트 (defaultRequeueRejected=true)")
@Disabled("현재 코드는 개선되어 예외를 던지지 않음. 이전 방식 문서화를 위해  남겨논 테스트")
class PaymentRefundInfiniteRetryTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentLogService paymentLogService;

    @Mock
    private TossPaymentClient tossPaymentClient;

    @Mock
    private PaymentRefundRetryPublisher refundRetryPublisher;

    private PaymentRefundConsumer consumer;

    User user;
    Auction auction;
    Payment payment;
    RefundRequestedMQEvent message;

    @BeforeEach
    void setUp() {
        // Consumer 생성
        consumer = new PaymentRefundConsumer(
                paymentRepository,
                paymentLogService,
                tossPaymentClient,
                refundRetryPublisher
        );

        user = UserFixture.generalUser();
        auction = AuctionFixture.openEndedAuction(
                PropertyFixture.property(user),
                LocalDateTime.now()
        );
        payment = PaymentFixture.successPayment(user, auction, PaymentType.DEPOSIT);
        ReflectionTestUtils.setField(payment, "id", 1L);
        payment.requestRefund();

        message = new RefundRequestedMQEvent(1L);
    }

    @Test
    @DisplayName("defaultRequeueRejected=true 시 네트워크 오류가 발생하면 무한 재시도됨을 시뮬레이션")
    void simulate_infinite_retry_when_requeue_rejected_true() {
        // given
        when(paymentRepository.getByIdOrThrow(1L)).thenReturn(payment);

        // Toss API가 계속 네트워크 오류를 발생시킴
        doThrow(new TossNetworkException("connection timeout"))
                .when(tossPaymentClient)
                .refund(any(), any(), any());

        AtomicInteger retryCount = new AtomicInteger(0);
        int maxSimulatedRetries = 100; // 무한 재시도를 100번으로 제한하여 시뮬레이션

        // when - RabbitMQ의 즉시 requeue 동작을 시뮬레이션
        // defaultRequeueRejected=true면 예외 발생 시 메시지가 즉시 큐로 돌아가서 다시 consume됨
        while (retryCount.get() < maxSimulatedRetries) {
            try {
                consumer.consume(message);
                break; // 성공하면 루프 종료
            } catch (TossNetworkException e) {
                retryCount.incrementAndGet();
                // defaultRequeueRejected=true: 예외 발생 → 즉시 requeue → 다시 consume
                // 이 루프가 무한히 반복됨 (실제 RabbitMQ에서는 CPU/메모리 폭주)
            }
        }

        // then
        assertThat(retryCount.get()).isEqualTo(maxSimulatedRetries);
        // 100번 재시도해도 상태는 변하지 않음 - 무한 루프
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUND_REQUESTED);

        // Toss API가 100번 호출됨 (실제로는 무한대)
        verify(tossPaymentClient, times(maxSimulatedRetries)).refund(any(), any(), any());
    }

    @Test
    @DisplayName("재시도 간격 없이 즉시 재시도되어 CPU 폭주 상황 시뮬레이션")
    void simulate_cpu_spike_with_immediate_retry() {
        // given
        when(paymentRepository.getByIdOrThrow(1L)).thenReturn(payment);

        doThrow(new TossNetworkException("connection timeout"))
                .when(tossPaymentClient)
                .refund(any(), any(), any());

        // when - 1초 동안 얼마나 많은 재시도가 발생하는지 측정
        long startTime = System.currentTimeMillis();
        int retryCount = 0;
        long duration = 100; // 100ms로 제한

        while (System.currentTimeMillis() - startTime < duration) {
            try {
                consumer.consume(message);
            } catch (TossNetworkException e) {
                retryCount++;
            }
        }

        // then
        // 100ms 동안 최소 10번 이상 재시도됨
        assertThat(retryCount).isGreaterThan(10);
    }

    @Test
    @DisplayName("여러 결제의 환불이 동시에 실패하면 시스템 전체가 멈춤")
    void simulate_system_freeze_with_multiple_failing_refunds() {
        // given
        Payment payment1 = PaymentFixture.successPayment(user, auction, PaymentType.DEPOSIT);
        Payment payment2 = PaymentFixture.successPayment(user, auction, PaymentType.DEPOSIT);
        Payment payment3 = PaymentFixture.successPayment(user, auction, PaymentType.DEPOSIT);

        ReflectionTestUtils.setField(payment1, "id", 1L);
        ReflectionTestUtils.setField(payment2, "id", 2L);
        ReflectionTestUtils.setField(payment3, "id", 3L);

        payment1.requestRefund();
        payment2.requestRefund();
        payment3.requestRefund();

        when(paymentRepository.getByIdOrThrow(1L)).thenReturn(payment1);
        when(paymentRepository.getByIdOrThrow(2L)).thenReturn(payment2);
        when(paymentRepository.getByIdOrThrow(3L)).thenReturn(payment3);

        doThrow(new TossNetworkException("connection timeout"))
                .when(tossPaymentClient)
                .refund(any(), any(), any());

        RefundRequestedMQEvent msg1 = new RefundRequestedMQEvent(1L);
        RefundRequestedMQEvent msg2 = new RefundRequestedMQEvent(2L);
        RefundRequestedMQEvent msg3 = new RefundRequestedMQEvent(3L);

        // when - 3개의 메시지가 각각 무한 재시도
        AtomicInteger totalRetries = new AtomicInteger(0);
        int maxPerMessage = 30;

        // 실제 RabbitMQ에서는 이 3개 메시지가 큐에서 무한 순환하며
        // Consumer 스레드를 100% 점유
        for (int i = 0; i < maxPerMessage; i++) {
            try { consumer.consume(msg1); } catch (TossNetworkException e) { totalRetries.incrementAndGet(); }
            try { consumer.consume(msg2); } catch (TossNetworkException e) { totalRetries.incrementAndGet(); }
            try { consumer.consume(msg3); } catch (TossNetworkException e) { totalRetries.incrementAndGet(); }
        }

        // then
        assertThat(totalRetries.get()).isEqualTo(maxPerMessage * 3);
        assertThat(payment1.getStatus()).isEqualTo(PaymentStatus.REFUND_REQUESTED);
        assertThat(payment2.getStatus()).isEqualTo(PaymentStatus.REFUND_REQUESTED);
        assertThat(payment3.getStatus()).isEqualTo(PaymentStatus.REFUND_REQUESTED);
    }
}
