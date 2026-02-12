package com.example.budongbudong.domain.bid.service;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.AuctionWinner;
import com.example.budongbudong.common.entity.Bid;
import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.domain.auction.event.AuctionClosedEvent;
import com.example.budongbudong.domain.auction.pubsub.AuctionClosedSubscriber;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.auctionwinner.repository.AuctionWinnerRepository;
import com.example.budongbudong.domain.bid.dto.response.CreateBidResponse;
import com.example.budongbudong.domain.bid.enums.BidStatus;
import com.example.budongbudong.domain.bid.repository.BidRepository;
import com.example.budongbudong.domain.payment.repository.PaymentRepository;
import com.example.budongbudong.domain.user.repository.UserRepository;
import com.example.budongbudong.fixture.AuctionFixture;
import com.example.budongbudong.fixture.PropertyFixture;
import com.example.budongbudong.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("네덜란드식 경매 입찰 → 경매 종료 + 낙찰자 확정 테스트")
class DutchBidServiceTest {

    @InjectMocks
    private BidService bidService;

    @Mock
    private BidRepository bidRepository;

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private User bidder;
    private Auction dutchAuction;
    @InjectMocks
    private AuctionClosedSubscriber subscriber;
    @Mock
    private AuctionWinnerRepository auctionWinnerRepository;

    @Mock
    private PaymentRepository paymentRepository;
    private Bid dutchBid;

    @BeforeEach
    void setUp() {
        User seller = UserFixture.sellerUser();
        bidder = UserFixture.generalUser();

        dutchAuction = AuctionFixture.openDutchAuction(
                PropertyFixture.property(seller),
                LocalDateTime.now().minusMinutes(30)
        );

        ReflectionTestUtils.setField(dutchAuction, "id", 1L);
        ReflectionTestUtils.setField(bidder, "id", 10L);

        dutchBid = Bid.builder()
                .user(bidder)
                .auction(dutchAuction)
                .price(BigDecimal.valueOf(90_000_000))
                .build();
        dutchBid.markHighest();
        ReflectionTestUtils.setField(dutchBid, "id", 100L);
    }

    @Test
    @DisplayName("입찰 시 현재가로 입찰이 생성되고 경매가 종료된다")
    void createsBidAtCurrentPriceAndClosesAuction() {

        // given
        when(userRepository.getByIdOrThrow(10L)).thenReturn(bidder);
        when(auctionRepository.getOpenAuctionOrThrow(1L)).thenReturn(dutchAuction);
        when(bidRepository.save(any(Bid.class))).thenAnswer(invocation -> {
            Bid bid = invocation.getArgument(0);
            ReflectionTestUtils.setField(bid, "id", 100L);
            return bid;
        });

        // when
        CreateBidResponse response = bidService.createDutchBid(1L, 10L);

        // then - 현재가로 입찰 생성 (100M - 30분(1회 하락) * 10M = 90M)
        BigDecimal expectedPrice = BigDecimal.valueOf(90_000_000);
        assertThat(response.getPrice()).isEqualByComparingTo(expectedPrice);
        assertThat(response.getBidStatus()).isEqualTo(BidStatus.WINNING);

        // then - 경매 종료
        verify(auctionRepository).closeIfOpen(1L);

        // then - 경매 종료 이벤트 발행
        verify(eventPublisher).publishEvent(any(AuctionClosedEvent.class));
    }

    @Test
    @DisplayName("네덜란드식 입찰의 낙찰자를 확정하고 Bid 상태를 WON으로 변경한다")
    void confirmsWinnerAndChangesBidStatusToWon() {

        // given
        when(auctionWinnerRepository.existsByAuctionId(1L)).thenReturn(false);
        when(bidRepository.findTopByAuctionIdOrderByPriceDescCreatedAtAsc(1L)).thenReturn(Optional.of(dutchBid));
        when(paymentRepository.findDepositPaymentIdsByAuctionIdAndNotWinnerUserId(1L, 10L)).thenReturn(List.of());

        // when
        subscriber.handleMessage(new AuctionClosedEvent(1L));

        //then - 낙찰자 확정
        verify(auctionWinnerRepository).save(any(AuctionWinner.class));

        // then - Bid 상태 WON으로 변경
        assertThat(dutchBid.getStatus()).isEqualTo(BidStatus.WON);

        // then - 네덜란드식은 입찰자가 1명이므로 환불 이벤트 미발행
        verify(eventPublisher, never()).publishEvent(any());
    }

}
