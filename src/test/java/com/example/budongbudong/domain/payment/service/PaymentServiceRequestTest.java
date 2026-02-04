package com.example.budongbudong.domain.payment.service;

import com.example.budongbudong.common.entity.*;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.payment.dto.response.PaymentTossReadyResponse;
import com.example.budongbudong.domain.payment.enums.PaymentStatus;
import com.example.budongbudong.domain.payment.enums.PaymentType;
import com.example.budongbudong.domain.payment.repository.PaymentRepository;
import com.example.budongbudong.domain.payment.toss.client.TossPaymentClient;
import com.example.budongbudong.domain.user.repository.UserRepository;
import com.example.budongbudong.fixture.*;
import com.example.budongbudong.fixture.PaymentFixture;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceRequestTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private TossPaymentClient  tossPaymentClient;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private UserRepository  userRepository;

    @InjectMocks
    private PaymentService paymentService;

    //fixture
    User user;
    Auction auction;
    Property property;

    @BeforeEach
    void setUp() {
        user = UserFixture.sellerUser();
        property = PropertyFixture.property(user);
        auction = AuctionFixture.openEndedAuction(property, LocalDateTime.now());

        //공통 stub
        when(auctionRepository.getAuctionWithPropertyOrTrow(auction.getId()))
                .thenReturn(auction);

        when(userRepository.getByIdOrThrow(user.getId()))
                .thenReturn(user);
    }

    @Test
    @DisplayName("이미 성공한 결제 타입으로 재요청하면 예외가 발생한다")
    void shouldThrowException_whenRequestingAlreadyPaidType(){

        //given
        Payment successPayment = PaymentFixture.successPayment(user,auction, PaymentType.DEPOSIT);

        //stub
        //성공한 결제 있음
        when(paymentRepository.findByAuctionAndTypeAndStatus(auction, PaymentType.DEPOSIT, PaymentStatus.SUCCESS))
                .thenReturn(Optional.of(successPayment));

        //when&then
        assertThatThrownBy(() -> paymentService.requestPayment(user.getId(), auction.getId(), PaymentType.DEPOSIT))
        .isInstanceOf(CustomException.class);

        verify(paymentRepository, never()).save(any());
        verify(tossPaymentClient, never()).confirm(anyString(), anyString(), any(BigDecimal.class));
    }

    @Test
    @DisplayName("진행 중 결제가 있으면 새 결제를 만들지 않고 기존 결제를 반환한다")
    void shouldReturnExistingPayment_whenInProgressPaymentExists() {

        //given
        Payment inprogressPayment = PaymentFixture.inprogressPayment(user,auction, PaymentType.DEPOSIT);

        //stub
        //성공한 결제는 없음
        when(paymentRepository.findByAuctionAndTypeAndStatus(auction, PaymentType.DEPOSIT, PaymentStatus.SUCCESS)).
                thenReturn(Optional.empty());
        //진행 중인 결제는 있음
        when(paymentRepository.findByAuctionAndTypeAndStatus(auction, PaymentType.DEPOSIT, PaymentStatus.IN_PROGRESS))
                .thenReturn(Optional.of(inprogressPayment));

        //when
        PaymentTossReadyResponse response = paymentService.requestPayment(user.getId(), auction.getId(), PaymentType.DEPOSIT);

        //then
        assertThat(response.getOrderId()).isEqualTo(inprogressPayment.getOrderId());
        verify(paymentRepository, never()).save(any());
    }
}