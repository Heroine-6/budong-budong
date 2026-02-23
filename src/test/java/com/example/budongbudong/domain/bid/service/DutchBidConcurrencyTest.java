package com.example.budongbudong.domain.bid.service;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.Bid;
import com.example.budongbudong.common.entity.Property;
import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.auction.event.AuctionClosedEvent;
import com.example.budongbudong.domain.auction.event.AuctionClosedEventListener;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.bid.repository.BidRepository;
import com.example.budongbudong.domain.property.repository.PropertyRepository;
import com.example.budongbudong.domain.user.repository.UserRepository;
import com.example.budongbudong.fixture.AuctionFixture;
import com.example.budongbudong.fixture.PropertyFixture;
import com.example.budongbudong.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@DisplayName("네덜란드식 경매 동시 입찰 동시성 테스트")
class DutchBidConcurrencyTest {

    private static final int USER_COUNT = 10;
    private static final BigDecimal EXPECTED_BID_PRICE = BigDecimal.valueOf(90_000_000);

    @Autowired
    private BidService bidService;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @MockitoSpyBean
    private AuctionClosedEventListener auctionClosedEventListener;

    private Long auctionId;
    private List<Long> userIds;

    @BeforeEach
    void setUp() {
        User seller = userRepository.save(UserFixture.sellerUser());

        userIds = IntStream.rangeClosed(1, USER_COUNT)
                .mapToObj(i -> UserFixture.generalUser())
                .map(userRepository::save)
                .map(User::getId)
                .toList();

        Property property = propertyRepository.save(PropertyFixture.property(seller));

        // 네덜란드 경매: 시작가 1억, 감가율 10%, 30분 전 시작 → 현재가 9천만
        Auction auction = AuctionFixture.openDutchAuction(property, LocalDateTime.now().minusMinutes(30));
        auction.updateStatus(AuctionStatus.OPEN);
        auctionId = auctionRepository.save(auction).getId();
    }

    @Test
    @DisplayName("N명이 동시에 입찰하면 정확히 1건만 성공하고 경매가 종료된다")
    void onlyOneBidSucceedsUnderConcurrency() throws InterruptedException {

        // given
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        CountDownLatch ready = new CountDownLatch(USER_COUNT);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(USER_COUNT);
        ExecutorService pool = Executors.newFixedThreadPool(USER_COUNT);

        // when
        for (Long userId : userIds) {
            pool.execute(() -> {
                ready.countDown();
                try {
                    start.await();
                    bidService.createDutchBid(auctionId, userId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        done.await();
        pool.shutdown();

        // then - 성공/실패 카운트
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(USER_COUNT - 1);

        // then - 경매 CLOSED 상태
        Auction auction = auctionRepository.findById(auctionId).orElseThrow();
        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.CLOSED);

        // then - 입찰 건수 1건 + 낙찰가 9천만원
        List<Bid> bids = bidRepository.findAllByAuctionId(auctionId, Pageable.unpaged()).getContent();
        assertThat(bids).hasSize(1);
        assertThat(bids.get(0).getPrice()).isEqualByComparingTo(EXPECTED_BID_PRICE);

        // then - 경매 종료 이벤트 발행
        verify(auctionClosedEventListener, times(1)).handleAuctionClosedEvent(any(AuctionClosedEvent.class));
    }
}