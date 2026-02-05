package com.example.budongbudong.domain.auction.service;

import com.example.budongbudong.domain.auction.event.AuctionClosedEvent;
import com.example.budongbudong.domain.auction.event.AuctionEndingSoonEvent;
import com.example.budongbudong.domain.auction.event.AuctionOpenEvent;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionSchedulerService {

    private final AuctionRepository auctionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final EntityManager entityManager;

    /**
     * 자정 기준 경매 상태 전환 책임을 가지는 메서드
     * - 시작 시간 도달 → SCHEDULED → OPEN
     * - 종료 시간 도달 → OPEN → ENDED
     * <p>
     * 처리 순서
     * 1. 시작 도달한 경매 OPEN
     * 2. OPEN 된 경매에 대해 이벤트 발행
     * 3. 종료일이 지난 경매 CLOSED
     * 4. CLOSED 된 경매에 대해 이벤트 발행
     */
    @Transactional
    public void run() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();

        openScheduledAuctions(todayStart);
        closeOpenedAuctions(todayStart);

        //벌크 연산은 영속성 컨텍스트 우회, 이후 조회 일관성을 위해 명시적으로 초기화
        entityManager.flush();
        entityManager.clear();
    }

    /**
     * 시작 도달한 경매를 OPEN 상태로 전환 및 이벤트 발행
     * - 벌크 업데이트 사용
     * - 단순 상태 전환 전용
     * - 실제 전환된 대상만 이벤트 발행
     */
    private void openScheduledAuctions(LocalDateTime today) {

        List<Long> willOpenAuctionIds = auctionRepository.findOpenAuctionIds(today);

        if (willOpenAuctionIds.isEmpty()) {
            log.info("[Scheduling] OPEN 대상 없음");
            return;
        }

        int opened = auctionRepository.openScheduled(willOpenAuctionIds);
        log.info("[경매 OPEN 처리] opened={}", opened);

        //이벤트 발행
        willOpenAuctionIds.forEach(id
                -> eventPublisher.publishEvent(new AuctionOpenEvent(id))
        );
    }

    /**
     * 종료일이 지난 경매를 CLOSED 상태로 전환하고,
     * 상태 전환이 발생한 경매에 대해 종료 이벤트를 발행
     * - 종료 대상 ID를 먼저 조회
     * - 벌크 업데이트로 상태 전환
     * - 실제 전환된 대상만 이벤트 발행
     */
    private void closeOpenedAuctions(LocalDateTime today) {

        List<Long> willCloseAuctionIds = auctionRepository.findEndedAuctionIds(today);

        if (willCloseAuctionIds.isEmpty()) {
            log.info("[Scheduling] CLOSED 대상 없음");
            return;
        }

        int closed = auctionRepository.closeOpened(willCloseAuctionIds);
        log.info("[Scheduling] CLOSE 처리 완료 - count={}", closed);

        //이벤트 발행
        willCloseAuctionIds.forEach(id -> eventPublisher.publishEvent(new AuctionClosedEvent(id)));
    }

    /**
     * 오늘이 종료일인 경매에 대해 알림 이벤트 발행
     * - 종료 임박 대상 경매 ID 조회
     * - 종료 임박 이벤트 발행
     */
    @Transactional(readOnly = true)
    public void notifyAuctionsEndingSoon() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();

        List<Long> endingSoonAuctionIds = auctionRepository.findEndingSoonAuctionIds(todayStart);

        if (endingSoonAuctionIds.isEmpty()) {
            log.info("[Scheduling] 종료 임박 대상 없음");
            return;
        }

        //이벤트 발행
        endingSoonAuctionIds.forEach(id
                -> eventPublisher.publishEvent(new AuctionEndingSoonEvent(id)));
    }
}
