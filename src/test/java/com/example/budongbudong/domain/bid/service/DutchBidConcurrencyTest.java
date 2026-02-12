package com.example.budongbudong.domain.bid.service;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.Bid;
import com.example.budongbudong.common.entity.Property;
import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.bid.enums.BidStatus;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("네덜란드식 경매 동시 입찰 정합성 테스트")
class DutchBidConcurrencyTest {

    int userNum = 10;

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

    private Long auctionId;
    private List<User> savedUsers;

    @BeforeEach
    void setUp() {

        User seller = UserFixture.sellerUser();
        userRepository.save(seller);

        List<User> users = IntStream.rangeClosed(1, userNum).mapToObj(i -> UserFixture.generalUser()).toList();
        savedUsers = userRepository.saveAll(users);

        Property property = PropertyFixture.property(seller);
        propertyRepository.save(property);

        // 네덜란드 경매: 시작가 1억, 하한가 5천만, 감가율 10%, 30분 전 시작
        Auction auction = AuctionFixture.openDutchAuction(property, LocalDateTime.now().minusMinutes(30));
        auction.updateStatus(AuctionStatus.OPEN);
        auctionRepository.save(auction);
        auctionId = auction.getId();
    }

    @Test
    @DisplayName("동시 입찰 시 정확히 1건만 성공하고 경매가 종료된다")
    void onlyOneBidSucceedsUnderConcurrency() throws InterruptedException {

        // When - N명이 동시에 입찰
        ConcurrencyResult result = runConcurrentDutchBids();

        // Then - 정합성 검증
        assertDutchBidInvariants();

        // 성공 횟수 = 1 (1명만 입찰 성공)
        assertThat(result.success()).isEqualTo(1);

        // 실패 횟수 >= 1 (경매 종료 후 입찰 시도한 스레드들은 실패)
        assertThat(result.fail()).isGreaterThanOrEqualTo(1);

        System.out.println("성공: " + result.success() + ", 실패: " + result.fail());
    }

    private ConcurrencyResult runConcurrentDutchBids() throws InterruptedException {

        List<Long> userIds = savedUsers.stream().map(User::getId).toList();

        ExecutorService executorService = Executors.newFixedThreadPool(userNum);
        CountDownLatch ready = new CountDownLatch(userNum);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(userNum);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        for (int i = 0; i < userNum; i++) {
            final long userId = userIds.get(i);

            executorService.execute(() -> {
                ready.countDown();
                try {
                    start.await();
                    bidService.createDutchBid(auctionId, userId);
                    success.incrementAndGet();
                } catch (Exception e) {
                    fail.incrementAndGet();
                    System.out.println("입찰 실패 = " + e.getMessage());
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        done.await();
        executorService.shutdown();

        return new ConcurrencyResult(success.get(), fail.get());
    }

    private void assertDutchBidInvariants() {

        // 경매 상태: CLOSED
        Auction auction = auctionRepository.findById(auctionId).orElseThrow();
        assertThat(auction.getStatus()).isEqualTo(AuctionStatus.CLOSED);

        List<Bid> bids = bidRepository.findAllByAuctionId(auctionId, Pageable.unpaged()).getContent();

        // 입찰 1건 존재
        assertThat(bids.size()).isEqualTo(1);

        // 존재하는 입찰이 Winning
        assertThat(bids.get(0).getStatus()).isEqualTo(BidStatus.WINNING);

        // isHighest = true 인 입찰이 정확히 1개
        long highestCount = bids.stream().filter(Bid::isHighest).count();
        assertThat(highestCount).isEqualTo(1);

        // 입찰가가 예상 현재가와 일치 (1억 - 10% * 1회 하락 = 9천만)
        BigDecimal expectedPrice = BigDecimal.valueOf(90_000_000);
        assertThat(bids.get(0).getPrice()).isEqualByComparingTo(expectedPrice);

    }

    private record ConcurrencyResult(int success, int fail) {
    }

}
