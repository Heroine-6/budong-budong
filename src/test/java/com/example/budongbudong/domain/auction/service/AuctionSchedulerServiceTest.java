package com.example.budongbudong.domain.auction.service;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.Property;
import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.bid.repository.BidRepository;
import com.example.budongbudong.domain.property.enums.PropertyType;
import com.example.budongbudong.domain.property.repository.PropertyRepository;
import com.example.budongbudong.domain.user.enums.UserRole;
import com.example.budongbudong.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class AuctionSchedulerServiceTest {
    @Autowired
    private AuctionSchedulerService auctionSchedulerService;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BidRepository bidRepository;

    private Property createProperty(User user) {
        Property property = Property.create(
                "아파투",
                "서울시 동대문구",
                1,
                10,
                2,
                PropertyType.APARTMENT,
                Year.of(2020),
                "설명",
                100000000L,
                LocalDate.now().plusMonths(1),
                new BigDecimal("84.50"),
                new BigDecimal("59.99"),
                user
        );
        return property;
    }

    private User createUser() {
        return User.create(
                "test@test.com",
                "tester",
                "password",
                "01012345678",
                "서울",
                UserRole.GENERAL
        );
    }

    @Test
    void startAt이_지난_경매는_OPEN_상태로_변경() {
        //given
        User user = userRepository.save(createUser());
        Property property = propertyRepository.save(createProperty(user));

        Auction auction = auctionRepository.save(
                Auction.create(
                        property,
                        100000000L,
                        LocalDateTime.now().minusMinutes(1),
                        LocalDateTime.now().plusMinutes(5)
                )
        );

        //when
        auctionSchedulerService.openScheduledAction();

        // then
        Auction result = auctionRepository.findById(auction.getId()).get();
        assertThat(result.getStatus()).isEqualTo(AuctionStatus.OPEN);
    }

}