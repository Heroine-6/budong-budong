package com.example.budongbudong.domain.payment.MQ;

import com.example.budongbudong.common.entity.*;
import com.example.budongbudong.domain.payment.dto.request.PaymentConfirmRequest;
import com.example.budongbudong.domain.payment.enums.PaymentStatus;
import com.example.budongbudong.domain.payment.enums.PaymentType;
import com.example.budongbudong.domain.payment.repository.PaymentRepository;
import com.example.budongbudong.domain.payment.toss.client.TossPaymentClient;
import com.example.budongbudong.domain.payment.toss.dto.response.TossPaymentStatusResponse;
import com.example.budongbudong.domain.payment.toss.enums.TossPaymentStatus;
import com.example.budongbudong.domain.payment.toss.exception.TossNetworkException;
import com.example.budongbudong.domain.payment.toss.utils.TossPaymentStatusMapper;
import com.example.budongbudong.fixture.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * VERIFYING 상태 결제에 대한 보정(재확인) 프로세스 테스트
 * - MQ / 배치 기반 내부 재확인 로직
 * - PG 상태 조회 후 SUCCESS / FAIL 전이
 * - 내부 식별자(paymentId) 기준으로 결제를 조회한다
 */
@ExtendWith(MockitoExtension.class)
public class PaymentVerifyConsumerTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private TossPaymentClient tossPaymentClient;

    @Mock
    private PaymentVerifyPublisher publisher;

    @Mock
    private TossPaymentStatusMapper mapper;

    @InjectMocks
    private PaymentVerifyConsumer consumer;

    User user;
    Auction auction;
    Payment payment;
    PaymentConfirmRequest request;
    PaymentVerifyMessage message;

    @BeforeEach
    void setUp() {
        //given
        user = UserFixture.sellerUser();
        auction = AuctionFixture.openEndedAuction(
                PropertyFixture.property(user),
                LocalDateTime.now()
        );
        payment = PaymentFixture.verifyingPayment(user,auction, PaymentType.DEPOSIT);
        request = new PaymentConfirmRequest(payment.getPaymentKey(), payment.getOrderId(), payment.getAmount());
        message = new PaymentVerifyMessage(payment.getId());
    }
    @Test
    @DisplayName("VERIFYING 상태에서 PG DONE 응답 시 SUCCESS로 전이된다.")
    void shouldMakeSuccess_whenTossReturnDone() {

        //given
        TossPaymentStatusResponse response = new TossPaymentStatusResponse("DONE");

        //stub
        when(paymentRepository.getByIdOrThrow(payment.getId())).thenReturn(payment);
        when(tossPaymentClient.getPayment(payment.getPaymentKey())).thenReturn(response);
        //토스 DONE으로 받아서 SUCCESS 상태로 매핑했음
        when(mapper.map(response)).thenReturn(TossPaymentStatus.SUCCESS);

        //when
        consumer.consume(message);

        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        verify(publisher, never()).publish(anyLong(), anyLong());
    }

    @Test
    @DisplayName("VERIFYING 상태에서 PG CANCELED 응답 시 FAIL로 전이된다.")
    void shouldMakeFail_whenTossReturnCanceled() {

        //given
        TossPaymentStatusResponse response = new TossPaymentStatusResponse("CANCELED");

        //stub
        when(paymentRepository.getByIdOrThrow(payment.getId())).thenReturn(payment);
        when(tossPaymentClient.getPayment(payment.getPaymentKey())).thenReturn(response);
        //토스 CANCELED으로 받아서 FAIL 상태로 매핑했음
        when(mapper.map(response)).thenReturn(TossPaymentStatus.FAIL);

        //when
        consumer.consume(message);

        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAIL);
        verify(publisher, never()).publish(anyLong(), anyLong());
    }

    @Test
    @DisplayName("VERIFYING 상태에서 일정 시간 경과하면 FAIL로 전이된다.")
    void shouldMakeFail_whenVerifyingTimeout() {

        //given
        ReflectionTestUtils.setField(payment, "verifyingStartedAt", LocalDateTime.now().minusMinutes(10)); // 타임아웃

        //stub
        when(paymentRepository.getByIdOrThrow(payment.getId())).thenReturn(payment);

        //when
        consumer.consume(message);

        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAIL);
        verify(publisher, never()).publish(anyLong(), anyLong());
        verify(tossPaymentClient, never()).getPayment(anyString());
    }

    @Test
    @DisplayName("PG 조회 중 장애가 발생하면 재시도 이벤트가 발행된다.")
    void shouldRetry_whenTossNetworkError() {

        //given
        when(tossPaymentClient.getPayment(payment.getPaymentKey())).thenThrow(new TossNetworkException("토스 장애"));

        //stub
        when(paymentRepository.getByIdOrThrow(payment.getId())).thenReturn(payment);

        //when
        consumer.consume(message);

        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.VERIFYING);
        verify(publisher).publish(payment.getId(), 30000L);
    }

    @Test
    @DisplayName("일시적인 PG 장애 후 정상 재확인되면 결제가 자동으로 SUCCESS 복구 된다.")
    void shouldRecoverToSuccess_afterTemporaryPgFailure() {

        //given
        TossPaymentStatusResponse response = new TossPaymentStatusResponse("DONE");

        //stub
        when(paymentRepository.getByIdOrThrow(payment.getId())).thenReturn(payment);
        //1~2회 PG 장애후 3회차에 정상적으로 성공 응답 들어온다면
        when(tossPaymentClient.getPayment(payment.getPaymentKey()))
                .thenThrow(new TossNetworkException("토스 장애 1회차"))
                .thenThrow(new TossNetworkException("토스 장애 2회차"))
                .thenReturn(new TossPaymentStatusResponse("DONE"));
        when(mapper.map(any(TossPaymentStatusResponse.class))).thenReturn(TossPaymentStatus.SUCCESS);

        //when-1회차
        consumer.consume(message);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.VERIFYING);

        //when-2회차
        consumer.consume(message);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.VERIFYING);

        //when-3회차 ( 이땐 정상 동작 )
        consumer.consume(message);

        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        //확정 전까지 retry 이벤트가 최소 2번 발행되었는지 검증
        verify(publisher, atLeast(2)).publish(payment.getId(), 30000L);

    }

    @Test
    @DisplayName("VERIFY_LIMIT 초과 시 결제는 FAIL로 확정되고 MQ 재발행이 중단된다")
    void shouldMakeFail_whenVerifyLimitTimeOut() {

        //given
        ReflectionTestUtils.setField(payment, "verifyingStartedAt", LocalDateTime.now().minusMinutes(10)); //타임아웃

        when(paymentRepository.getByIdOrThrow(payment.getId())).thenReturn(payment);

        //when
        consumer.consume(message);

        //then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAIL);
        //재시도 중단
        verify(publisher, never()).publish(anyLong(), anyLong());
    }

}
