package com.example.budongbudong.domain.auction.scheduler;

import com.example.budongbudong.domain.auction.service.AuctionSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionScheduler {

    private final AuctionSchedulerService auctionSchedulerService;

    /**
     * 경매 상태 전환을 자정에 트리거하는 스케줄러
     * - 매일 자정(00:00)에 배치를 실행하는 진입점
     * - 실제 상태 변경 책임은 service에 있음
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul") // 자정
    public void runMidnightBatch() {
        log.info("[AuctionScheduler] 자정 경매 처리 시작");
        auctionSchedulerService.run();
        log.info("[AuctionScheduler] 자정 경매 처리 완료");
    }

    /**
     * 경매 종료 임박 알림을 23시에 트리거하는 스케줄러
     * - 매일 23시(23:00)에 종료 임박 경매를 조회
     */
    @Scheduled(cron = "0 0 23 * * *", zone = "Asia/Seoul") // 23시
    public void checkAuctionsEndingSoon() {
        log.info("[AuctionScheduler] 종료 임박 경매 탐색 시작");
        auctionSchedulerService.notifyAuctionsEndingSoon();
        log.info("[AuctionScheduler] 종료 임박 경매 탐색 완료");
    }

    /**
     * 네덜란드 경매 감가 스케쥴러
     * - 매일 경매 시작(자정) 후 30분마다 트리거
     */
    @Scheduled(cron = "0 */30 0-23 * * *", zone = "Asia/Seoul")
    public void decreaseDutchAuctionPrice() {
        log.info("[AuctionScheduler] 네덜란드식 경매 감가 및 유찰 처리 시작");
        auctionSchedulerService.decreaseDutchAuctionPrice();
        log.info("[AuctionScheduler] 네덜란드식 경매 감가 및 유찰 처리 완료");
    }

}
