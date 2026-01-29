package com.example.budongbudong.domain.property.realdeal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;

/**
 * 실거래가 데이터 파이프라인 오케스트레이터
 *
 * 전체 흐름:
 * 1. 공공데이터 API에서 실거래가 수집 → RDB 저장
 * 2. 주소를 좌표로 변환 (지오코딩) → RDB 업데이트
 * 3. 좌표가 포함된 데이터를 Elasticsearch에 인덱싱
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DealPipelineService {

    private final DealCollectService dealCollectService;
    private final DealGeoCodingService dealGeoCodingService;
    private final DealIndexService dealIndexService;

    /**
     * 전체 파이프라인 실행
     * @param lawdCodes 법정동 코드 목록 (예: "11110" = 종로구)
     * @param from 수집 시작 월
     * @param to 수집 종료 월
     */
    public void runFullPipeline(List<String> lawdCodes, YearMonth from, YearMonth to) {
        log.info("[파이프라인 시작] codes={}, from={}, to={}", lawdCodes.size(), from, to);

        // 1단계: 공공데이터 수집
        long t0 = System.currentTimeMillis();
        for (String lawdCd : lawdCodes) {
            try {
                dealCollectService.collect(lawdCd, from, to);
            } catch (Exception e) {
                log.error("[수집 실패] lawdCd={}", lawdCd, e);
            }
        }
        long t1 = System.currentTimeMillis();

        // 2단계: 지오코딩
        int geocoded = dealGeoCodingService.geocodeBatch();
        long t2 = System.currentTimeMillis();

        // 3단계: ES 인덱싱
        int indexed = dealIndexService.indexAll();
        long t3 = System.currentTimeMillis();
    }
}
