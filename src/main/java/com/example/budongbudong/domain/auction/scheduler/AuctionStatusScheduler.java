package com.example.budongbudong.domain.auction.scheduler;

import com.example.budongbudong.domain.auction.service.AuctionSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 매 정각마다 실행
 * - 시작 시간이 지난 경매: SCHEDULED → OPEN
 * - 종료 시간이 지난 경매: OPEN → CLOSED
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionStatusScheduler {

    private final AuctionSchedulerService auctionSchedulerService;

    @Scheduled(cron="0 0 * * * *")
    public void updateAuctionStatusAtHour(){
        LocalDateTime now = LocalDateTime.now();
        log.info("[AuctionScheduler] 경매 상태 변경 시작 - now = {}", now);

        auctionSchedulerService.openScheduledAction();
        auctionSchedulerService.closeEndedAction();

        log.info("[AuctionScheduler] 경매 상태 변경 완료");
    }
}
