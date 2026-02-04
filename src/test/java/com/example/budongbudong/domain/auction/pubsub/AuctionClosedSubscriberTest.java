package com.example.budongbudong.domain.auction.pubsub;

import com.example.budongbudong.common.entity.*;
import com.example.budongbudong.domain.auction.event.AuctionClosedEvent;
import com.example.budongbudong.domain.auction.event.DepositRefundEvent;
import com.example.budongbudong.domain.auctionwinner.repository.AuctionWinnerRepository;
import com.example.budongbudong.domain.bid.repository.BidRepository;
import com.example.budongbudong.domain.payment.enums.PaymentType;
import com.example.budongbudong.domain.payment.event.RefundRequestDomainEvent;
import com.example.budongbudong.domain.payment.repository.PaymentRepository;
import com.example.budongbudong.fixture.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("AuctionClosedSubscriber 낙찰자 확정 + 환불 이벤트 발행 테스트")
class AuctionClosedSubscriberTest {

    @InjectMocks
    private AuctionClosedSubscriber auctionClosedSubscriber;

    @Mock
    private AuctionWinnerRepository auctionWinnerRepository;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    User seller;
    User bidder;
    Auction auction;
    Bid bid;
    Payment payment;

    @BeforeEach
    void setUp() {
        seller = UserFixture.sellerUser();
        bidder = UserFixture.generalUser();
        auction = AuctionFixture.openEndedAuction(
                PropertyFixture.property(seller),
                LocalDateTime.now()
        );
        bid = BidFixture.bid(bidder,auction);
    }

    @Nested
    @DisplayName("handleMessage")
    class HandleMessage {

        @Test
        @DisplayName("최고 입찰자를 낙찰자로 확정하고 탈락자 환불 이벤트를 발행한다")
        void winner_confirmed_and_refund_event() {
            //given
            //stub 1 - 낙찰자 없고
            when(auctionWinnerRepository.existsByAuctionId(auction.getId()))
                    .thenReturn(false);
            //stub 2 - 최고 입찰 있고
            when(bidRepository.findTopByAuctionIdOrderByPriceDescCreatedAtAsc(auction.getId()))
                    .thenReturn(Optional.of(bid));
            // stub 3 - 탈락자 보증금 존재 하고
            when(paymentRepository.findDepositPaymentIdsByAuctionIdAndNotWinnerUserId(auction.getId(), bidder.getId()))
                .thenReturn(List.of(1L, 2L));

            //when
            handleMessage(auction.getId());
            //then
            verify(auctionWinnerRepository).save(any(AuctionWinner.class));
            verify(eventPublisher).publishEvent(any(DepositRefundEvent.class));
        }

        @Test
        @DisplayName("이미 낙찰자가 존재하면 중복 처리하지 않는다 (멱등성)")
        void already_winner_skip() {
            //given
            //stub 1 - 낙찰자 존재 하고
            when(auctionWinnerRepository.existsByAuctionId(auction.getId()))
                    .thenReturn(false);

            //when
            handleMessage(auction.getId());
            //then
            verify(eventPublisher, never()).publishEvent(any());
            verify(auctionWinnerRepository, never()).save(any());
            verify(bidRepository, never()).findTopByAuctionIdOrderByPriceDescCreatedAtAsc(anyLong());
        }

        @Test
        @DisplayName("입찰이 없으면 낙찰자를 생성하지 않고 이벤트도 발행하지 않는다")
        void no_bids_no_event() {

            //given
            //stub 1 - 낙찰자 없고
            when(auctionWinnerRepository.existsByAuctionId(auction.getId()))
                    .thenReturn(false);
            //stub 2 - 입찰 없고
            when(bidRepository.findTopByAuctionIdOrderByPriceDescCreatedAtAsc(auction.getId()))
                    .thenReturn(Optional.empty());

            //when
            handleMessage(auction.getId());
            //then
            verify(eventPublisher, never()).publishEvent(any());
            verify(auctionWinnerRepository, never()).save(any());
        }

        @Test
        @DisplayName("낙찰자만 있고 탈락자가 없으면 환불 이벤트를 발행하지 않는다")
        void only_winner_no_refund_event() {
            //given
            //stub 1 - 낙찰자 아직 없음
            when(auctionWinnerRepository.existsByAuctionId(auction.getId()))
                    .thenReturn(false);
            //stub 2 - 최고 입찰자 존제
            when(bidRepository.findTopByAuctionIdOrderByPriceDescCreatedAtAsc(auction.getId()))
                    .thenReturn(Optional.of(bid));
            //stub 3 - 환불 진행할 보즘금 없고
            when(paymentRepository.findDepositPaymentIdsByAuctionIdAndNotWinnerUserId(auction.getId(), bidder.getId()))
                    .thenReturn(List.of());

            //when
            handleMessage(auction.getId());

            //then
            verify(eventPublisher, never()).publishEvent(any());
            verify(auctionWinnerRepository).save(any());
        }

        private void handleMessage(Long auctionId) {
            auctionClosedSubscriber.handleMessage(new AuctionClosedEvent(auctionId));
        }
    }
}
