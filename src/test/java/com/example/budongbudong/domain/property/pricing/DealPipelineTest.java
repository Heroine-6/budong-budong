package com.example.budongbudong.domain.property.pricing;

import com.example.budongbudong.domain.property.pricing.repository.RealDealRepository;
import com.example.budongbudong.domain.property.pricing.service.DealCollectService;
import com.example.budongbudong.domain.property.pricing.service.DealGeoCodingService;
import com.example.budongbudong.domain.property.pricing.service.DealIndexService;
import com.example.budongbudong.domain.property.pricing.service.DealPipelineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DealPipelineTest {

    @Autowired
    DealPipelineService pipelineService;

    @Autowired
    DealCollectService collectService;

    @Autowired
    DealGeoCodingService geoCodingService;

    @Autowired
    DealIndexService indexService;

    @Autowired
    RealDealRepository realDealRepository;

    @BeforeEach
    void setUp() {
        // 테스트 전 데이터 초기화 (이전 실행의 latitude=0 데이터 제거)
        realDealRepository.deleteAll();
    }

    @Test
    @DisplayName("전체 파이프라인 테스트 - 종로구 2024년 1월")
    void 파이프라인_전체_테스트() {
        // given
        String lawdCd = "11110"; // 종로구
        YearMonth targetMonth = YearMonth.of(2024, 1);

        // when
        pipelineService.runFullPipeline(
                List.of(lawdCd),
                targetMonth,
                targetMonth
        );

        // then
        long totalCount = realDealRepository.count();
        long geocodedCount = totalCount - realDealRepository.countByLatitudeIsNull();

        System.out.println("=== 파이프라인 결과 ===");
        System.out.println("총 수집: " + totalCount + "건");
        System.out.println("지오코딩 완료: " + geocodedCount + "건");

        assertThat(totalCount).isGreaterThan(0);
    }

    @Test
    @DisplayName("1단계 : 공공데이터 수집 테스트")
    void 공공데이터_수집_테스트() {
        // given
        String lawdCd = "11110"; // 종로구
        YearMonth targetMonth = YearMonth.of(2024, 1);

        // when
        collectService.collect(lawdCd, targetMonth, targetMonth);

        // then
        long count = realDealRepository.count();
        System.out.println("수집된 데이터: " + count + "건");

        assertThat(count).isGreaterThan(0);
    }

    @Test
    @DisplayName("2단계 : 지오코딩 테스트 (수집 후 실행)")
    void 지오코딩_테스트() {
        // given - 먼저 수집
        String lawdCd = "11110";
        YearMonth targetMonth = YearMonth.of(2024, 1);
        collectService.collect(lawdCd, targetMonth, targetMonth);

        long beforeNull = realDealRepository.countByLatitudeIsNull();
        System.out.println("지오코딩 전 미처리: " + beforeNull + "건");

        // when
        int geocoded = geoCodingService.geocodeBatch();

        // then
        long afterNull = realDealRepository.countByLatitudeIsNull();
        System.out.println("지오코딩 완료: " + geocoded + "건");
        System.out.println("지오코딩 후 미처리: " + afterNull + "건");

        // 도로명 주소 검증
        long withRoadAddress = realDealRepository.countByRoadAddressIsNotNull();
        System.out.println("도로명 주소 변환: " + withRoadAddress + "건");

        assertThat(geocoded).isGreaterThan(0);
        assertThat(withRoadAddress).isGreaterThan(0);
    }

    @Test
    @DisplayName("3단계 : ES 인덱싱 테스트 (수집+지오코딩 후 실행)")
    void ES_인덱싱_테스트() {
        // given - 먼저 수집 + 지오코딩
        String lawdCd = "11110";
        YearMonth targetMonth = YearMonth.of(2024, 1);
        collectService.collect(lawdCd, targetMonth, targetMonth);
        geoCodingService.geocodeBatch();

        // when
        int indexed = indexService.indexAll();

        // then
        System.out.println("ES 인덱싱 완료: " + indexed + "건");

        assertThat(indexed).isGreaterThan(0);
    }
}
