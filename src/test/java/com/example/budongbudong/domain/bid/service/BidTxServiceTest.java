package com.example.budongbudong.domain.bid.service;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.Property;
import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.bid.dto.request.CreateBidRequest;
import com.example.budongbudong.domain.property.enums.PropertyType;
import com.example.budongbudong.domain.property.repository.PropertyRepository;
import com.example.budongbudong.domain.user.enums.UserRole;
import com.example.budongbudong.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class BidTxServiceTest {

    @Autowired
    private BidTxService bidTxService;
    @Autowired
    private AuctionRepository auctionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PropertyRepository propertyRepository;

    private Long auctionId;
    private Long userId1;
    private Long userId2;

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

        User user1 = User.create(
                "user1-" + runId + "@test.com",
                "user1",
                "password",
                "010-0000-0001",
                "서울 어딘가",
                UserRole.GENERAL
        );
        User user2 = User.create(
                "user2-" + runId + "@test.com",
                "user2",
                "password",
                "010-0000-0002",
                "서울 어딘가",
                UserRole.GENERAL
        );
        userRepository.save(user1);
        userRepository.save(user2);
        userId1 = user1.getId();
        userId2 = user2.getId();

        Property property = Property.builder()
                .name("테스트 매물")
                .address("서울특별시 강남구 역삼동 123-45")
                .floor(10)
                .totalFloor(20)
                .roomCount(3)
                .type(PropertyType.APARTMENT)
                .builtYear(Year.of(2015))
                .description("테스트 매물 설명입니다.")
                .price(BigDecimal.valueOf(500_000_000L))
                .migrateDate(LocalDate.now().plusDays(7))
                .supplyArea(new BigDecimal("84.32"))
                .privateArea(new BigDecimal("59.12"))
                .user(seller)
                .build();
        propertyRepository.save(property);

        Auction auction = Auction.create(
                property,
                BigDecimal.valueOf(1_000_000L),
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(10)
        );
        auction.updateStatus(AuctionStatus.OPEN);
        auctionRepository.save(auction);
        auctionId = auction.getId();
    }

    @Test
    @DisplayName("첫 입찰이 최소 입찰 단위 배수가 아니면 실패")
    void firstBidMustMatchIncrement() {
        CreateBidRequest request = createRequest(BigDecimal.valueOf(1_124_000L));

        assertThatThrownBy(() -> bidTxService.createBidTx(request, auctionId, userId1))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_BID_PRICE);
    }

    @Test
    @DisplayName("이후 입찰이 최소 입찰 단위 배수가 아니면 실패")
    void nextBidMustMatchIncrement() {
        CreateBidRequest validFirst = createRequest(BigDecimal.valueOf(1_100_000L));
        bidTxService.createBidTx(validFirst, auctionId, userId1);

        CreateBidRequest invalidNext = createRequest(BigDecimal.valueOf(1_240_000L));

        assertThatThrownBy(() -> bidTxService.createBidTx(invalidNext, auctionId, userId2))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_BID_PRICE);
    }

    private CreateBidRequest createRequest(BigDecimal price) {
        CreateBidRequest request = new CreateBidRequest();
        ReflectionTestUtils.setField(request, "price", price);
        return request;
    }
}
