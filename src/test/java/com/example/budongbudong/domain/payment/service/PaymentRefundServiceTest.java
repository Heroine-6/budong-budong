package com.example.budongbudong.domain.payment.service;

import com.example.budongbudong.common.entity.*;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.domain.payment.enums.PaymentStatus;
import com.example.budongbudong.domain.payment.enums.PaymentType;
import com.example.budongbudong.domain.payment.repository.PaymentRepository;
import com.example.budongbudong.fixture.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService 환불 요청 테스트")
class PaymentRefundServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    User user;
    Auction auction;
    Payment payment;

    @BeforeEach
    void setUp() {
        //given
        user = UserFixture.generalUser();
        auction = AuctionFixture.openEndedAuction(
                PropertyFixture.property(user),
                LocalDateTime.now()
        );
        payment = PaymentFixture.successPayment(user, auction, PaymentType.DEPOSIT);
    }

    @Nested
    @DisplayName("환불 요청")
    class RequestRefund {

        @Test
        @DisplayName("SUCCESS 상태의 DEPOSIT 결제를 환불 요청하면 REFUND_REQUESTED로 전이된다")
        void refund_success_deposit() {
            //when
            payment.requestRefund();

            //then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUND_REQUESTED);
        }

        @Test
        @DisplayName("DEPOSIT이 아닌 결제는 환불 요청 시 예외가 발생한다")
        void refund_non_deposit_throws() {
            //given
            Payment payment = PaymentFixture.successPayment(user, auction, PaymentType.DOWN_PAYMENT);

            //when then
            assertThatThrownBy(payment::requestRefund)
                    .isInstanceOf(CustomException.class);
        }

        @Test
        @DisplayName("SUCCESS가 아닌 결제는 환불 요청 시 예외가 발생한다")
        void refund_non_success_throws() {
            //given
            Payment payment = PaymentFixture.inprogressPayment(user, auction, PaymentType.DEPOSIT);

            //when then
            assertThatThrownBy(payment::requestRefund)
                    .isInstanceOf(CustomException.class);
        }
    }

    @Nested
    @DisplayName("내가 결제한 부분에 대해서만 환불을 요청한다")
    class RequestRefundByUser {

        @Test
        @DisplayName("본인의 결제만 환불 요청할 수 있다")
        void refund_own_payment() {
            //given
            when(paymentRepository.getByIdAndUserIdOrThrow(payment.getId(), user.getId()))
                    .thenReturn(payment);

            //when
            paymentService.requestRefund(user.getId(), payment.getId());

            //then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUND_REQUESTED);
        }

        @Test
        @DisplayName("타인의 결제를 환불 요청하면 FORBIDDEN 예외가 발생한다")
        void refund_other_user_throws() {
            //given
            User otherUser = UserFixture.user2();
            when(paymentRepository.getByIdAndUserIdOrThrow(payment.getId(), user.getId()))
                    .thenThrow(CustomException.class);

            //when then
            assertThatThrownBy(() -> paymentService.requestRefund(otherUser.getId(), payment.getId()))
                    .isInstanceOf(CustomException.class);
        }
    }
}
