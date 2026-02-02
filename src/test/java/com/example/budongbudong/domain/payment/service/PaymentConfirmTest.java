package com.example.budongbudong.domain.payment.service;

import com.example.budongbudong.common.entity.*;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.domain.payment.MQ.*;
import com.example.budongbudong.domain.payment.dto.request.PaymentConfirmRequest;
import com.example.budongbudong.domain.payment.enums.PaymentStatus;
import com.example.budongbudong.domain.payment.enums.PaymentType;
import com.example.budongbudong.domain.payment.repository.PaymentRepository;
import com.example.budongbudong.domain.payment.toss.client.TossPaymentClient;
import com.example.budongbudong.domain.payment.toss.exception.TossNetworkException;
import com.example.budongbudong.fixture.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 결제 승인(confirm) 요청에 대한 서비스 테스트
 * - 토스 결제 승인 요청 처리
 * - 금액 검증, 멱등성, 상태 전이(SUCCESS / FAIL / VERIFYING)
 * - 외부 요청 기준(orderId)으로 결제를 조회한다
 */
@ExtendWith(MockitoExtension.class)
public class PaymentConfirmTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private TossPaymentClient tossPaymentClient;

    @Mock
    private PaymentVerifyPublisher publisher;

    @InjectMocks
    private PaymentService paymentService;

    private static final String PAYMENT_KEY = "paymentKey-test-123";

    User user;
    Auction auction;
    Payment payment;
    PaymentConfirmRequest request;

    @BeforeEach
    void setUp() {
        //given
        user = UserFixture.user();
        auction = AuctionFixture.openEndedAuction(
                PropertyFixture.property(user),
                LocalDateTime.now()
        );
        payment = PaymentFixture.inprogressPayment(user,auction, PaymentType.DEPOSIT);
        request = createRequest(payment);

        //stub : 진행 중인 결과 있음
        when(paymentRepository.getByOrderIdOrThrow(payment.getOrderId())).thenReturn(payment);
    }

    @Test
    @DisplayName("결제 승인 성공하면 결제 상태가 SUCCESS로 전이된다")
    void shouldMakePaymentSuccess_whenTossConfirmSucceed() {

        //stub : 진행 중인 결과 있음
        when(paymentRepository.getByOrderIdOrThrow(payment.getOrderId()))
                .thenReturn(payment);

        //toss confirm 성공
        doNothing().when(tossPaymentClient).confirm(anyString(), anyString(), any(BigDecimal.class));

        //when
        paymentService.confirmPayment(request);

        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(PAYMENT_KEY).isEqualTo(payment.getPaymentKey());
        verify(tossPaymentClient).confirm(request.paymentKey(), request.orderId(), request.amount());
    }

    @Test
    @DisplayName("요청 금액이 다르면 결제는 FAIL 처리되고 예외가 발생한다")
    void shouldFailPayment_whenAmountMismatch() {

        //given - 금액 다른 요청
        PaymentConfirmRequest invalidRequest =
                new PaymentConfirmRequest(request.paymentKey(), request.orderId(), request.amount().add(BigDecimal.ONE));

        //stub : 진행 중인 결과 있음
        when(paymentRepository.getByOrderIdOrThrow(payment.getOrderId()))
                .thenReturn(payment);

        //when&then
        assertThatThrownBy(() -> paymentService.confirmPayment(invalidRequest))
                .isInstanceOf(CustomException.class);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAIL);
        verify(tossPaymentClient, never()).confirm(anyString(), anyString(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("Toss 장애 시 결제는 VERIFYING 상태로 전이되고 MQ가 발행된다.")
    void shouldMakeVerifying_whenTossError() {

        //stub : 진행 중인 결과 있음
        when(paymentRepository.getByOrderIdOrThrow(payment.getOrderId()))
                .thenReturn(payment);

        doThrow(new TossNetworkException("토스 장애"))
                .when(tossPaymentClient)
                .confirm(anyString(), anyString(), any(BigDecimal.class));

        //when
        paymentService.confirmPayment(request);

        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.VERIFYING);
        verify(publisher).publish(payment.getId(), 30000L);
    }

    @Test
    @DisplayName("PG 승인 성공 후 서버가 결제 결과를 확정하지 못하면 VERIFYING으로 전이한다.")
    void shouldMakeVerifying_whenServerCannotConfirmPaymentAfterPgSuccess() {

        //stub : 진행 중인 결과 있음
        when(paymentRepository.getByOrderIdOrThrow(payment.getOrderId()))
                .thenReturn(payment);

        //toss는 성공
        doNothing().when(tossPaymentClient).confirm(anyString(), anyString(), any(BigDecimal.class));
        //서버 내부 처리 중 예외 발생
        doThrow(new RuntimeException("트랜잭션 롤백, 영속화 실패 등등"))
                .when(paymentRepository)
                .save(any());

        //when - 예외는 흡수, 이후 프로세스에 맡김
        paymentService.confirmPayment(request);

        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.VERIFYING);
        //이후 PG 상태 재확인 대상
        verify(publisher).publish(payment.getId(), 30000L);

    }

    @Test
    @DisplayName("이미 SUCCESS 상태인 결제에 동일한 confirm 요청이 와도 멱등하게 처리한다.")
    void shouldBeIdempotent_whenConfirmRequestAgainAfterSuccess() {

        //given
        payment = PaymentFixture.successPayment(user,auction, PaymentType.DEPOSIT);
        PaymentConfirmRequest request = createRequest(payment);
        //stub : 진행 중인 결과 있음
        when(paymentRepository.getByOrderIdOrThrow(payment.getOrderId()))
                .thenReturn(payment);

        //when - 동일한 요청이 오면
        paymentService.confirmPayment(request);

        //then
        verify(tossPaymentClient, never()).confirm(anyString(), anyString(), any(BigDecimal.class));
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        verify(publisher, never()).publish(payment.getId(), 30000L);

    }

    @Test
    @DisplayName("VERIFYING 상태의 결제에 confirm 요청이 와도 멱등하게 처리된다.")
    void shouldIgnoreConfirm_whenNowVerifying() {

        //given
        Payment payment = PaymentFixture.verifyingPayment(user,auction, PaymentType.DEPOSIT);
        PaymentConfirmRequest request = createRequest(payment);

        when(paymentRepository.getByOrderIdOrThrow(payment.getOrderId())).thenReturn(payment);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.VERIFYING);

        //when
        paymentService.confirmPayment(request);

        //then
        verify(tossPaymentClient, never()).confirm(anyString(), anyString(), any(BigDecimal.class));
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.VERIFYING);
        // 중복 publish 안됨
        verify(publisher, never()).publish(payment.getId(), 30000L);

    }

    private PaymentConfirmRequest createRequest(Payment payment) {
        return new PaymentConfirmRequest(payment.getPaymentKey(), payment.getOrderId(), payment.getAmount());
    }
}
