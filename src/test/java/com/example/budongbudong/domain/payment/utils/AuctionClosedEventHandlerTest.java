package com.example.budongbudong.domain.payment.utils;

import com.example.budongbudong.domain.payment.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuctionClosedEventHandler DepositRefundEvent 처리 테스트")
class AuctionClosedEventHandlerTest {

    @InjectMocks
    private AuctionClosedEventHandler auctionClosedEventHandler;

    @Mock
    private PaymentService paymentService;

    @Nested
    @DisplayName("handle")
    class Handle {

        @Test
        @DisplayName("탈락자 결제 ID 목록 각각에 대해 requestRefund를 호출한다")
        void calls_refund_for_each_loser() {
        }

        @Test
        @DisplayName("탈락자 목록이 비어있으면 requestRefund를 호출하지 않는다")
        void empty_list_no_call() {
        }
    }
}
