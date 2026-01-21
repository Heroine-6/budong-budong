package com.example.budongbudong.domain.bid.service;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.Bid;
import com.example.budongbudong.common.entity.Property;
import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.auctionwinner.repository.AuctionWinnerRepository;
import com.example.budongbudong.domain.bid.dto.request.CreateBidRequest;
import com.example.budongbudong.domain.bid.repository.BidRepository;
import com.example.budongbudong.domain.property.enums.PropertyType;
import com.example.budongbudong.domain.property.repository.PropertyRepository;
import com.example.budongbudong.domain.propertyimage.repository.PropertyImageRepository;
import com.example.budongbudong.domain.user.enums.UserRole;
import com.example.budongbudong.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BidServiceConcurrencyTest {

    @Autowired
    private BidService bidService;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private AuctionWinnerRepository auctionWinnerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private PropertyImageRepository propertyImageRepository;

    private Long auctionId;
    private List<User> savedUsers;

    int userNum = 10;

    @BeforeEach
    void setUp() {

        String runId = String.valueOf(System.nanoTime());

        User seller = User.create(
                "seller-" + runId + "@test.com",
                "seller",
                "password",
                "010-1111-1111",
                "서울시 강남구",
                UserRole.SELLER
        );
        userRepository.save(seller);

        List<User> users = IntStream.rangeClosed(1, userNum)
                .mapToObj(i ->
                        User.create(
                                "user" + i + "-" + runId + "@test.com",
                                "user" + i,
                                "password",
                                "010-0000-000" + i,
                                "서울 어딘가",
                                UserRole.GENERAL
                        )).toList();
        savedUsers = userRepository.saveAll(users);

        Property property = Property.builder()
                .name("테스트 매물")
                .address("서울특별시 강남구 역삼동 123-45")
                .floor(10)
                .totalFloor(20)
                .roomCount(3)
                .type(PropertyType.APARTMENT)
                .builtYear(Year.of(2015))
                .description("테스트 매물 설명입니다.")
                .price(500_000_000L)
                .migrateDate(LocalDate.now().plusDays(7))
                .supplyArea(new BigDecimal("84.32"))
                .privateArea(new BigDecimal("59.12"))
                .user(seller)
                .build();
        propertyRepository.save(property);

        Auction auction = Auction.create(
                property,
                1_000L,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(1)
        );
        auction.updateStatus(AuctionStatus.OPEN);
        auctionRepository.save(auction);
        auctionId = auction.getId();
    }

    @Test
    @DisplayName("동시 입찰 발생 시 비관적 락 실행")
    void Pessimistic_Lock_Test() throws InterruptedException {

        // Given
        List<Long> userIds = savedUsers.stream()
                .map(User::getId)
                .toList();

        ExecutorService executorService = Executors.newFixedThreadPool(userNum);
        CountDownLatch ready = new CountDownLatch(userNum);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(userNum);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        // When
        for (int i = 0; i < userNum; i++) {

            final long userId = userIds.get(i);
            final long bidPrice = 2000 + (i * 100);

            executorService.execute(() -> {
                ready.countDown();
                try {
                    start.await();
                    CreateBidRequest bidRequest = new CreateBidRequest();
                    ReflectionTestUtils.setField(bidRequest, "price", bidPrice);
                    bidService.createBid(bidRequest, auctionId, userId);
                    success.incrementAndGet();
                } catch (Exception e) {
                    fail.incrementAndGet();
                    System.out.println("입찰 실패 = " + e);
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        done.await();
        executorService.shutdown();

        // Then
//        assertThat(success.get()).isEqualTo(userNum);
//        assertThat(fail.get()).isEqualTo(0);

        List<Bid> bids = bidRepository.findAllByAuctionId(auctionId, Pageable.unpaged()).getContent();

        long highestCount = bids.stream().filter(Bid::isHighest).count();
        assertThat(highestCount).isEqualTo(1);

        long maxPrice = bids.stream().mapToLong(Bid::getPrice).max().orElseThrow();
        assertThat(maxPrice).isEqualTo(2900);

        Bid highestBid = bids.stream().filter(Bid::isHighest).findFirst().orElseThrow();
        assertThat(highestBid.getPrice()).isEqualTo(maxPrice);
    }
}