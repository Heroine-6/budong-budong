package com.example.budongbudong.domain.property.realdeal.service;

import com.example.budongbudong.common.entity.RealDeal;
import com.example.budongbudong.domain.property.realdeal.client.KakaoGeoClient;
import com.example.budongbudong.domain.property.realdeal.client.KakaoGeoResponse;
import com.example.budongbudong.domain.property.realdeal.client.NaverGeoClient;
import com.example.budongbudong.domain.property.realdeal.client.NaverGeoResponse;
import com.example.budongbudong.domain.property.realdeal.enums.GeoStatus;
import com.example.budongbudong.domain.property.realdeal.repository.RealDealRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 주소 → 좌표 변환 (지오코딩) 서비스
 *
 * - 네이버 지오코딩 API 우선 사용
 * - 네이버 실패 시 카카오 API로 폴백
 * - 실패 유형에 따라 FAILED/RETRY 상태 구분
 *
 * 실패 분류:
 * - FAILED: 주소 자체 문제 (200 + 빈 결과) → 재시도 X
 * - RETRY: 일시적 오류 (401/403/429/5xx/timeout) → 나중에 재시도
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DealGeoCodingService {

    private final RealDealRepository realDealRepository;
    private final NaverGeoClient naverGeoClient;
    private final KakaoGeoClient kakaoGeoClient;

    private static final int BATCH_SIZE = 500;
    private static final long THROTTLE_MS = 50;
    private static final int MAX_RETRY = 3;

    /**
     * PENDING 상태 신규 건 지오코딩
     */
    @Transactional
    public int geocodePending() {
        return geocodeByStatus(GeoStatus.PENDING, false);
    }

    /**
     * RETRY 상태 재시도 (최대 3회)
     */
    @Transactional
    public int geocodeRetry() {
        return geocodeByStatus(GeoStatus.RETRY, true);
    }

    private int geocodeByStatus(GeoStatus status, boolean isRetry) {
        int totalGeocoded = 0;

        while (true) {
            List<RealDeal> batch;
            if (isRetry) {
                batch = realDealRepository.findByGeoStatusAndRetryCountLessThan(
                        status, MAX_RETRY, PageRequest.of(0, BATCH_SIZE));
            } else {
                batch = realDealRepository.findByGeoStatus(status, PageRequest.of(0, BATCH_SIZE));
            }

            if (batch.isEmpty()) break;

            for (RealDeal deal : batch) {
                try {
                    GeoResult result = tryGeoCode(deal);

                    switch (result) {
                        case SUCCESS -> totalGeocoded++;
                        case ADDRESS_NOT_FOUND -> {
                            deal.markGeoFailed(true);  // 영구 실패
                            log.warn("[지오코딩 FAILED] 주소 못 찾음 - id={}, address={}",
                                    deal.getId(), deal.getAddress());
                        }
                        case TEMPORARY_ERROR -> {
                            deal.markGeoFailed(false); // 재시도 대상
                            if (deal.getRetryCount() >= MAX_RETRY) {
                                deal.markExhausted();
                                log.warn("[지오코딩 FAILED] 재시도 횟수 초과 - id={}, address={}",
                                        deal.getId(), deal.getAddress());
                            } else {
                                log.info("[지오코딩 RETRY] 일시적 오류 - id={}, retry={}/{}",
                                        deal.getId(), deal.getRetryCount(), MAX_RETRY);
                            }
                        }
                    }
                    Thread.sleep(THROTTLE_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return totalGeocoded;
                }
            }

            long remaining = isRetry
                    ? realDealRepository.countByGeoStatusAndRetryCountLessThan(status, MAX_RETRY)
                    : realDealRepository.countByGeoStatus(status);
            log.info("[지오코딩 배치] status={}, geocoded={}, remaining≈{}",
                    status, totalGeocoded, remaining);
        }

        return totalGeocoded;
    }

    private enum GeoResult {
        SUCCESS,           // 좌표 획득 성공
        ADDRESS_NOT_FOUND, // 주소 못 찾음 (영구 실패)
        TEMPORARY_ERROR    // 일시적 오류 (재시도 대상)
    }

    private GeoResult tryGeoCode(RealDeal deal) {
        // 1. 네이버 시도
        GeoResult naverResult = tryNaverGeoCode(deal);
        if (naverResult == GeoResult.SUCCESS) return GeoResult.SUCCESS;

        // 2. 카카오 시도
        GeoResult kakaoResult = tryKakaoGeoCode(deal);
        if (kakaoResult == GeoResult.SUCCESS) return GeoResult.SUCCESS;

        // 3. 둘 다 "주소 못 찾음"이면 영구 실패
        if (naverResult == GeoResult.ADDRESS_NOT_FOUND && kakaoResult == GeoResult.ADDRESS_NOT_FOUND) {
            return GeoResult.ADDRESS_NOT_FOUND;
        }

        // 4. 하나라도 일시적 오류면 재시도 대상
        return GeoResult.TEMPORARY_ERROR;
    }

    private GeoResult tryNaverGeoCode(RealDeal deal) {
        try {
            NaverGeoResponse response = naverGeoClient.geocode(deal.getAddress());
            if (response.hasResult()) {
                NaverGeoResponse.Address addr = response.addresses().get(0);
                deal.applyGeoCode(
                        new BigDecimal(addr.y()),
                        new BigDecimal(addr.x()),
                        addr.roadAddress()
                );
                return GeoResult.SUCCESS;
            }
            // 200 + 빈 결과 = 주소 못 찾음
            return GeoResult.ADDRESS_NOT_FOUND;
        } catch (Exception e) {
            log.warn("[네이버 지오코딩 예외] address={}, error={}", deal.getAddress(), e.getMessage());
            return GeoResult.TEMPORARY_ERROR;
        }
    }

    private GeoResult tryKakaoGeoCode(RealDeal deal) {
        try {
            KakaoGeoResponse response = kakaoGeoClient.geocode(deal.getAddress());
            if (response.hasResult()) {
                KakaoGeoResponse.Document doc = response.documents().get(0);
                deal.applyGeoCode(
                        new BigDecimal(doc.y()),
                        new BigDecimal(doc.x()),
                        doc.addressName()
                );
                return GeoResult.SUCCESS;
            }
            return GeoResult.ADDRESS_NOT_FOUND;
        } catch (Exception e) {
            log.warn("[카카오 지오코딩 예외] address={}, error={}", deal.getAddress(), e.getMessage());
            return GeoResult.TEMPORARY_ERROR;
        }
    }
}
