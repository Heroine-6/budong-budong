package com.example.budongbudong.domain.auction.scheduler;

import com.example.budongbudong.domain.auction.service.AuctionSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 경매 상태 전환을 자정에 트리거하는 스케줄러
 * - 매일 자정(00:00)에 배치를 실행하는 진입점
 * - 실제 상태 변경 책임은 service에 있음
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionStatusScheduler {

    private final AuctionSchedulerService auctionSchedulerService;

    @Scheduled(cron = "0 0 0 * * *")
    public void runMidnightBatch() {
        log.info("[AuctionStatusScheduler] 자정 경매 처리 시작");
        auctionSchedulerService.run();
        log.info("[AuctionStatusScheduler] 자정 경매 처리 완료");
    }
}
