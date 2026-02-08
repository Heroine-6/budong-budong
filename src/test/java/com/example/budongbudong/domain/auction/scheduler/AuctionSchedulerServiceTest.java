package com.example.budongbudong.domain.auction.scheduler;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.domain.auction.event.AuctionClosedEvent;
import com.example.budongbudong.domain.auction.event.AuctionOpenEvent;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.auction.service.AuctionSchedulerService;
import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuctionSchedulerServiceTest {

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private AuctionSchedulerService auctionSchedulerService;

    LocalDateTime today;

    @BeforeEach
    void setUp() {
        today = LocalDate.now().atStartOfDay();
    }

    @Test
    @DisplayName("시작 처리 성공, 종료 처리 실패 : 시작 처리는 커밋됨")
    void testPartialSuccess_StartSuccess_EndFail() {
        //given
        List<Long> openIds = Arrays.asList(1L, 2L, 3L, 4L);
        when(auctionRepository.findOpenAuctionIds(today))
                .thenReturn(openIds);
        when(auctionRepository.openScheduled(openIds))
                .thenReturn(4);

        List<Long> closeIds = Arrays.asList(5L, 6L, 7L, 8L);
        when(auctionRepository.findEndedAuctionIds(today))
                .thenReturn(closeIds);
        when(auctionRepository.closeOpened(openIds))
                .thenThrow(new RuntimeException("DB Connection Error"));

        //when
        assertThatThrownBy(()-> {
            // 시작 처리 트랜잭션
            auctionSchedulerService.openScheduledAuctions(today);
            // 종료 처리 트랜잭션 ( 실패 되야 함)
            auctionSchedulerService.closeOpenedAuctions(today);
        }).isInstanceOf(RuntimeException.class);

        //then 1- 시작 처리
        verify(auctionRepository, times(1)).findOpenAuctionIds(today);
        verify(auctionRepository, times(1)).openScheduled(openIds);

        //시작 이벤트 4건 발행 확인
        ArgumentCaptor<AuctionOpenEvent> openEventCaptor = ArgumentCaptor.forClass(AuctionOpenEvent.class);
        verify(eventPublisher, times(4)).publishEvent(openEventCaptor.capture());

        List<AuctionOpenEvent> openEvents = openEventCaptor.getAllValues();
        assertThat(openEvents).hasSize(4);
        assertThat(openEvents.stream().map(AuctionOpenEvent::auctionId))
                .containsExactlyInAnyOrder(1L, 2L, 3L, 4L);

        //flush,clear 호출 확인
        verify(entityManager, times(1)).flush();
        verify(entityManager, times(1)).clear();

        //then 2 - 종료 처리
        verify(auctionRepository, times(1)).findEndedAuctionIds(today);
        verify(auctionRepository, times(1)).closeOpened(closeIds);

        verify(eventPublisher, never()).publishEvent(any(AuctionClosedEvent.class));
    }

    @Test
    @DisplayName("시작 처리 실패, 종료 처리 성공 - 종료는 커밋됨")
    void testPartialSuccess_StartFail_EndSuccess() {
        //given
        when(auctionRepository.findOpenAuctionIds(today))
                .thenThrow(new RuntimeException("DB read timeout"));

        //when
        assertThatThrownBy(()-> {
            auctionSchedulerService.openScheduledAuctions(today);
        }).isInstanceOf(RuntimeException.class);

        //then - 시작 이벤트 발행 안됨
        verify(eventPublisher, never()).publishEvent(any(AuctionClosedEvent.class));

        List<Long> closeIds = Arrays.asList(5L, 6L, 7L, 8L);
        when(auctionRepository.findEndedAuctionIds(today))
                .thenReturn(closeIds);
        when(auctionRepository.closeOpened(closeIds))
                .thenReturn(3);

        //when
        auctionSchedulerService.closeOpenedAuctions(today);

        //then 1- 종료 처리 성공
        verify(auctionRepository, times(1)).closeOpened(closeIds);
        //2- 종료 이벤트 4건 발행
        ArgumentCaptor<AuctionClosedEvent> closeEventCaptor =
                ArgumentCaptor.forClass(AuctionClosedEvent.class);
        verify(eventPublisher, times(4)).publishEvent(closeEventCaptor.capture());

        List<AuctionClosedEvent> closeEvents = closeEventCaptor.getAllValues();
        assertThat(closeEvents).hasSize(4);
        assertThat(closeEvents.stream().map(AuctionClosedEvent::auctionId))
                .containsExactlyInAnyOrder(5L, 6L, 7L, 8L);
    }
}
