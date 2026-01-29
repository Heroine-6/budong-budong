package com.example.budongbudong.domain.property.realdeal.service;

import com.example.budongbudong.common.entity.RealDeal;
import com.example.budongbudong.domain.property.realdeal.client.KakaoGeoClient;
import com.example.budongbudong.domain.property.realdeal.client.KakaoGeoResponse;
import com.example.budongbudong.domain.property.realdeal.client.NaverGeoClient;
import com.example.budongbudong.domain.property.realdeal.client.NaverGeoResponse;
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
 * - 둘 다 실패 시 (0, 0) 저장하여 재처리 방지
 * - 도로명 주소도 함께 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DealGeoCodingService {

    private final RealDealRepository realDealRepository;
    private final NaverGeoClient naverGeoClient;
    private final KakaoGeoClient kakaoGeoClient;

    private static final int BATCH_SIZE = 500;   // 한 번에 처리할 건수 (메모리/쿼터 고려)
    private static final long THROTTLE_MS = 50;  // API 호출 간격 (레이트 리밋 완화용)

    // 트랜잭션 범위 안에서 엔티티 업데이트를 모아 dirty checking으로 일괄 반영
    @Transactional
    public int geocodeBatch() {
        int totalGeocoded = 0;

        while (true) {
            // 아직 좌표가 없는 건만 배치로 가져와서 외부 API 호출량을 최소화
            List<RealDeal> batch = realDealRepository.findByLatitudeIsNull(
                    PageRequest.of(0, BATCH_SIZE)
            );
            if (batch.isEmpty()) break;

            for (RealDeal deal : batch) {
                try {
                    boolean success = tryNaverGeoCode(deal);
                    if (!success) {
                        success = tryKakaoGeoCode(deal);
                    }
                    if (!success) {
                        // 재시도 대상에서 제외하기 위해 실패 건은 (0,0)로 마킹
                        log.warn("[지오코딩 실패] id={}, address={}", deal.getId(), deal.getAddress());
                        deal.applyGeoCode(BigDecimal.ZERO, BigDecimal.ZERO, null);
                    } else {
                        totalGeocoded++;
                    }
                    Thread.sleep(THROTTLE_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("[지오코딩 중단]", e);
                    return totalGeocoded;
                } catch (Exception e) {
                    log.error("[지오코딩 에러] id={}, address={}", deal.getId(), deal.getAddress(), e);
                }
            }

            log.info("[지오코딩 배치] geocoded={}, remaining≈{}",
                    totalGeocoded, realDealRepository.countByLatitudeIsNull());
        }

        return totalGeocoded;
    }

    private boolean tryNaverGeoCode(RealDeal deal) {
        try {
            NaverGeoResponse response = naverGeoClient.geocode(deal.getAddress());
            log.debug("[네이버 응답] address={}, status={}, hasResult={}",
                    deal.getAddress(), response.status(), response.hasResult());
            if (response.hasResult()) {
                NaverGeoResponse.Address addr = response.addresses().get(0);
                deal.applyGeoCode(
                        new BigDecimal(addr.y()),
                        new BigDecimal(addr.x()),
                        addr.roadAddress()
                );
                return true;
            }
        } catch (Exception e) {
            log.warn("[네이버 지오코딩 예외] address={}, error={}", deal.getAddress(), e.getMessage());
        }
        return false;
    }

    private boolean tryKakaoGeoCode(RealDeal deal) {
        try {
            KakaoGeoResponse response = kakaoGeoClient.geocode(deal.getAddress());
            log.debug("[카카오 응답] address={}, hasResult={}", deal.getAddress(), response.hasResult());
            if (response.hasResult()) {
                KakaoGeoResponse.Document doc = response.documents().get(0);
                deal.applyGeoCode(
                        new BigDecimal(doc.y()),
                        new BigDecimal(doc.x()),
                        doc.addressName()
                );
                return true;
            }
        } catch (Exception e) {
            log.warn("[카카오 지오코딩 예외] address={}, error={}", deal.getAddress(), e.getMessage());
        }
        return false;
    }
}
