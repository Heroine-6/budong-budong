package com.example.budongbudong.domain.auction.service;

import com.example.budongbudong.common.entity.*;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.auction.pubsub.AuctionClosedPublisher;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.property.repository.PropertyRepository;
import com.example.budongbudong.domain.user.repository.UserRepository;
import com.example.budongbudong.fixture.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class AuctionStatusServiceTest {
    @Autowired
    AuctionSchedulerService auctionSchedulerService;

    @Autowired
    AuctionRepository auctionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PropertyRepository propertyRepository;

    @MockitoBean
    AuctionClosedPublisher auctionClosedPublisher;

    @Test
    void 자정_배치_종료일이_지난_경매만_CLOSED() {

        //given
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        User user = userRepository.save(UserFixture.user());
        Property property = propertyRepository.save(PropertyFixture.property(user));

        Auction endedAuction = auctionRepository.save(
                AuctionFixture.openEndedAuction(property, todayStart)
        );

        Auction ongoingAuction = auctionRepository.save(
                AuctionFixture.openOngoingAuction(property, todayStart)
        );

        //when
        auctionSchedulerService.run();

        //then
        Auction closed = auctionRepository.findById(endedAuction.getId()).orElseThrow();
        Auction stillOpen = auctionRepository.findById(ongoingAuction.getId()).orElseThrow();

        assertThat(closed.getStatus()).isEqualTo(AuctionStatus.CLOSED);
        assertThat(stillOpen.getStatus()).isEqualTo(AuctionStatus.OPEN);
    }

    @Test
    void 종료된_경매에_대해서만_redis_이벤트가_발행된다() {

        // given
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        User user = userRepository.save(UserFixture.user());
        Property property = propertyRepository.save(PropertyFixture.property(user));

        Auction endedAuction = auctionRepository.save(AuctionFixture.openEndedAuction(property, startOfToday));

        // when
        auctionSchedulerService.run();

        // then
        //Redis Pub/Sub으로 전달된 auctionId를 검증하기 위한 캡처
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);

        //Redis 이벤트가 정확히 한 번 발행됐는지
        verify(auctionClosedPublisher, times(1)).publish(captor.capture());
        //Redis로 실제 전송된 auctionId
        Long publishedAuctionId = captor.getValue();

        assertThat(publishedAuctionId).isEqualTo(endedAuction.getId());
    }

}