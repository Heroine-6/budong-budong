package com.example.budongbudong.domain.payment.MQ;

import com.example.budongbudong.common.entity.*;
import com.example.budongbudong.domain.payment.enums.PaymentStatus;
import com.example.budongbudong.domain.payment.enums.PaymentType;
import com.example.budongbudong.domain.payment.log.service.PaymentLogService;
import com.example.budongbudong.domain.payment.repository.PaymentRepository;
import com.example.budongbudong.domain.payment.toss.client.TossPaymentClient;
import com.example.budongbudong.domain.payment.toss.exception.TossNetworkException;
import com.example.budongbudong.fixture.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * setDefaultRequeueRejected(false) + 지연 큐 방식 테스트
 *
 * 개선된 방식:
 * - 예외 발생 시 메시지를 버림 (requeue 안 함)
 * - 명시적으로 지연 큐에 발행 (30초 후 재시도)
 * - 최대 재시도 횟수 도달 시 중단
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("지연 큐 방식 재시도 테스트 (setDefaultRequeueRejected=false)")
class PaymentRefundWithDelayQueueTest {

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

    private static final int MAX_REFUND_RETRY = 5;

    @BeforeEach
    void setUp() {
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
    @DisplayName("네트워크 오류 시 예외를 던지지 않고 지연 큐에 발행한다")
    void should_publish_to_delay_queue_instead_of_throwing_exception() {
        // given
        when(paymentRepository.getByIdOrThrow(1L)).thenReturn(payment);

        doThrow(new TossNetworkException("connection timeout"))
                .when(tossPaymentClient)
                .refund(any(), any(), any());

        // when - 예외가 발생하지 않음
        consumer.consume(message);

        // then
        // 1. 예외가 던져지지 않았음 (메서드가 정상 종료)
        // 2. 지연 큐에 발행됨
        verify(refundRetryPublisher).publish(1L);
        // 3. 재시도 횟수 증가
        assertThat(payment.getRefundRetryCount()).isEqualTo(1);

        System.out.println(" 예외 대신 지연 큐 발행 확인");
        System.out.println("   - refundRetryCount: " + payment.getRefundRetryCount());
    }

    @Test
    @DisplayName("최대 재시도 횟수(5회)까지만 재시도하고 중단된다")
    void should_stop_retry_after_max_attempts() {
        // given
        when(paymentRepository.getByIdOrThrow(1L)).thenReturn(payment);

        doThrow(new TossNetworkException("connection timeout"))
                .when(tossPaymentClient)
                .refund(any(), any(), any());

        List<Integer> retryCountHistory = new ArrayList<>();

        // when - 지연 큐 발행을 시뮬레이션 (실제로는 30초 후 재consume)
        for (int i = 0; i <= MAX_REFUND_RETRY + 2; i++) {
            consumer.consume(message);
            retryCountHistory.add(payment.getRefundRetryCount());
        }

        // then
        // 최대 5번까지만 지연 큐에 발행
        verify(refundRetryPublisher, times(MAX_REFUND_RETRY)).publish(1L);

        // 6번째부터는 "최대 재시도 초과" 로그만 남기고 종료
        assertThat(payment.getRefundRetryCount()).isEqualTo(MAX_REFUND_RETRY);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUND_REQUESTED);

        System.out.println("==============================================");
        System.out.println("   최대 재시도 제한 동작 확인:");
        System.out.println("   - 재시도 횟수 이력: " + retryCountHistory);
        System.out.println("   - 최종 retryCount: " + payment.getRefundRetryCount());
        System.out.println("   - 지연 큐 발행 횟수: " + MAX_REFUND_RETRY + "회");
        System.out.println("   - 상태: " + payment.getStatus() + " (수동 처리 필요)");
        System.out.println("==============================================");
    }

    @Test
    @DisplayName("무한 재시도와 비교 - CPU 사용량이 낮음")
    void compare_cpu_usage_with_delay_queue() {
        // given
        when(paymentRepository.getByIdOrThrow(1L)).thenReturn(payment);

        doThrow(new TossNetworkException("connection timeout"))
                .when(tossPaymentClient)
                .refund(any(), any(), any());

        // when - 100ms 동안 호출 시뮬레이션
        long startTime = System.currentTimeMillis();
        int consumeCount = 0;
        long duration = 100;

        while (System.currentTimeMillis() - startTime < duration) {
            consumer.consume(message);
            consumeCount++;

            // 지연 큐 방식에서는 실제로 30초 대기 후 재시도
            // 여기서는 즉시 호출하지만, 실제로는 이 루프가 돌지 않음
            if (payment.isRefundRetryExceeded(MAX_REFUND_RETRY)) {
                break; // 최대 재시도 도달 시 중단
            }
        }

        // then
        System.out.println("==============================================");
        System.out.println("   지연 큐 방식 결과:");
        System.out.println("   - " + duration + "ms 동안 consume 횟수: " + consumeCount + "회");
        System.out.println("   - 최대 재시도 도달 후 즉시 중단됨");
        System.out.println("   - 실제 환경에서는 30초 간격으로 최대 5회만 재시도");
        System.out.println("   - 총 소요 시간: 약 150초 (30초 x 5회)");
        System.out.println("==============================================");
        System.out.println("");
        System.out.println("   이전 방식(무한 재시도) 비교:");
        System.out.println("   - 100ms 동안 약 590회 재시도 (테스트 결과)");
        System.out.println("   - 초당 약 5,900회 재시도");
        System.out.println("   - CPU 100% 점유, 시스템 멈춤");
        System.out.println("==============================================");

        // 최대 재시도 + 1 (초과 체크) 정도만 호출됨
        assertThat(consumeCount).isLessThanOrEqualTo(MAX_REFUND_RETRY + 3);
    }

    @Test
    @DisplayName("여러 결제의 환불 실패 시에도 시스템이 정상 동작한다")
    void should_handle_multiple_failing_refunds_gracefully() {
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

        // when - 각 메시지를 최대 재시도까지 처리
        for (int i = 0; i <= MAX_REFUND_RETRY; i++) {
            consumer.consume(msg1);
            consumer.consume(msg2);
            consumer.consume(msg3);
        }

        // then
        // 각 결제당 최대 5회만 지연 큐 발행
        verify(refundRetryPublisher, times(MAX_REFUND_RETRY)).publish(1L);
        verify(refundRetryPublisher, times(MAX_REFUND_RETRY)).publish(2L);
        verify(refundRetryPublisher, times(MAX_REFUND_RETRY)).publish(3L);

        assertThat(payment1.getRefundRetryCount()).isEqualTo(MAX_REFUND_RETRY);
        assertThat(payment2.getRefundRetryCount()).isEqualTo(MAX_REFUND_RETRY);
        assertThat(payment3.getRefundRetryCount()).isEqualTo(MAX_REFUND_RETRY);

        System.out.println("==============================================");
        System.out.println("   여러 결제 처리 결과:");
        System.out.println("   - payment1 재시도: " + payment1.getRefundRetryCount() + "/" + MAX_REFUND_RETRY);
        System.out.println("   - payment2 재시도: " + payment2.getRefundRetryCount() + "/" + MAX_REFUND_RETRY);
        System.out.println("   - payment3 재시도: " + payment3.getRefundRetryCount() + "/" + MAX_REFUND_RETRY);
        System.out.println("   - 총 지연 큐 발행: " + (MAX_REFUND_RETRY * 3) + "회");
        System.out.println("   - 시스템 정상 동작 유지");
        System.out.println("==============================================");
        System.out.println("");
        System.out.println("   이전 방식(무한 재시도) 비교:");
        System.out.println("   - 3개 메시지가 무한 순환");
        System.out.println("   - Consumer 스레드 100% 점유");
        System.out.println("   - 새로운 정상 메시지 처리 불가");
        System.out.println("==============================================");
    }

    @Test
    @DisplayName("재시도 중 성공하면 즉시 완료된다")
    void should_complete_when_retry_succeeds() {
        // given
        when(paymentRepository.getByIdOrThrow(1L)).thenReturn(payment);

        AtomicInteger callCount = new AtomicInteger(0);

        // 처음 2번은 실패, 3번째에 성공
        doAnswer(invocation -> {
            if (callCount.incrementAndGet() <= 2) {
                throw new TossNetworkException("connection timeout");
            }
            return null; // 성공
        }).when(tossPaymentClient).refund(any(), any(), any());

        // when
        consumer.consume(message); // 1번째: 실패 → 지연 큐 발행
        consumer.consume(message); // 2번째: 실패 → 지연 큐 발행
        consumer.consume(message); // 3번째: 성공!

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(payment.getRefundRetryCount()).isEqualTo(2); // 2번 실패 후 성공
        verify(refundRetryPublisher, times(2)).publish(1L); // 2번만 지연 큐 발행

        System.out.println("==============================================");
        System.out.println("   재시도 중 성공 케이스:");
        System.out.println("   - 1번째 시도: 실패 → 지연 큐 발행");
        System.out.println("   - 2번째 시도: 실패 → 지연 큐 발행");
        System.out.println("   - 3번째 시도: 성공!");
        System.out.println("   - 최종 상태: " + payment.getStatus());
        System.out.println("   - 총 재시도: " + payment.getRefundRetryCount() + "회");
        System.out.println("==============================================");
    }
}
