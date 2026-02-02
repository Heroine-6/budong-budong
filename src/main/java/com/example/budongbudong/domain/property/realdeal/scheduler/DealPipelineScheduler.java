package com.example.budongbudong.domain.property.realdeal.scheduler;

import com.example.budongbudong.domain.property.lawdcode.LawdCodeService;
import com.example.budongbudong.domain.property.realdeal.service.DealGeoCodingService;
import com.example.budongbudong.domain.property.realdeal.service.DealIndexService;
import com.example.budongbudong.domain.property.realdeal.service.DealPipelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;

/**
 * 실거래가 데이터 파이프라인 스케줄러
 *
 * 스케줄:
 * - 월간 (매월 1일 03:00): 전월 데이터 수집 → 지오코딩 → ES 인덱싱
 * - 재시도 (매일 04:00): RETRY 상태 건 재처리 → ES 인덱싱
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DealPipelineScheduler {

    private final DealPipelineService dealPipelineService;
    private final DealGeoCodingService dealGeoCodingService;
    private final DealIndexService dealIndexService;
    private final LawdCodeService lawdCodeService;

    /**
     * 월간 파이프라인 실행 (매월 1일 03:00)
     * - 전월 실거래가 데이터 수집 → 지오코딩 → ES 인덱싱
     */
    @Scheduled(cron = "0 0 3 1 * *")
    public void runMonthlyPipeline() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        List<String> lawdCodes = lawdCodeService.getAllLawdCodes();
        dealPipelineService.runFullPipeline(lawdCodes, lastMonth, lastMonth);
        log.info("[스케줄러] 월간 파이프라인 완료 - {}", lastMonth);
    }

    /**
     * 지오코딩 재시도 (매일 04:00)
     * - RETRY 상태 건 재처리 (최대 3회)
     * - 성공 시 ES 인덱싱
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void runGeoRetry() {
        log.info("[스케줄러] 지오코딩 재시도 시작");
        int retried = dealGeoCodingService.geocodeRetry();
        if (retried > 0) {
            int indexed = dealIndexService.indexAll();
            log.info("[스케줄러] 재시도 완료 - geocoded={}, indexed={}", retried, indexed);
        } else {
            log.info("[스케줄러] 재시도 대상 없음");
        }
    }
}
