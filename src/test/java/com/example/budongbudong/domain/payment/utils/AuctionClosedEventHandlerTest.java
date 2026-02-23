package com.example.budongbudong.domain.payment.utils;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.domain.auction.event.DepositRefundEvent;
import com.example.budongbudong.domain.payment.service.PaymentService;
import com.example.budongbudong.fixture.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuctionClosedEventHandler DepositRefundEvent 처리 테스트")
class AuctionClosedEventHandlerTest {

    @InjectMocks
    private AuctionClosedEventHandler auctionClosedEventHandler;

    @Mock
    private PaymentService paymentService;

    User user;
    Auction auction;

    @BeforeEach
    void setUp() {
        user = UserFixture.generalUser();
        auction = AuctionFixture.openEndedAuction(
                PropertyFixture.property(user),
                LocalDateTime.now()
        );
    }

    @Nested
    @DisplayName("handle")
    class Handle {

        @Test
        @DisplayName("탈락자 결제 ID 목록 각각에 대해 requestRefund를 호출한다")
        void calls_refund_for_each_loser() {
            //given
            DepositRefundEvent event = new DepositRefundEvent(auction.getId(), user.getId(), List.of(1L,2L,3L));

            //when
            auctionClosedEventHandler.handle(event);

            //then
            verify(paymentService).requestRefund(1L);
            verify(paymentService).requestRefund(2L);
            verify(paymentService).requestRefund(3L);

        }

        @Test
        @DisplayName("탈락자 목록이 비어있으면 requestRefund를 호출하지 않는다")
        void empty_list_no_call() {
            //given
            DepositRefundEvent event = new DepositRefundEvent(auction.getId(), user.getId(), List.of());

            //when
            auctionClosedEventHandler.handle(event);

            //then
            verifyNoInteractions(paymentService);
        }
    }
}
