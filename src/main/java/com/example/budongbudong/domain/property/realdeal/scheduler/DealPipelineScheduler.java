package com.example.budongbudong.domain.property.realdeal.scheduler;

import com.example.budongbudong.domain.property.realdeal.service.DealPipelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;

/**
 * 실거래가 데이터 파이프라인 스케줄러
 * - 매월 1일 03:00에 전월 데이터 자동 수집
 * - 서울 25개 구 대상
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DealPipelineScheduler {

    private final DealPipelineService dealPipelineService;

    /** 서울 25개 구 법정동 코드 */
    private static final List<String> SEOUL_LAWD_CODES = List.of(
            "11110", "11140", "11170", "11200", "11215",
            "11230", "11260", "11290", "11305", "11320",
            "11350", "11380", "11410", "11440", "11470",
            "11500", "11530", "11545", "11560", "11590",
            "11620", "11650", "11680", "11710", "11740"
    );

    /**
     * 월간 파이프라인 실행 (매월 1일 03:00)
     * - 전월 실거래가 데이터 수집 → 지오코딩 → ES 인덱싱
     */
    @Scheduled(cron = "0 0 3 1 * *")  // 초 분 시 일 월 요일
    public void runMonthlyPipeline() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        dealPipelineService.runFullPipeline(SEOUL_LAWD_CODES, lastMonth, lastMonth);
        log.info("[DealPipelineScheduler] 월간 파이프라인 완료 - {}", lastMonth);
    }
}
