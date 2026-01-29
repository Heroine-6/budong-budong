package com.example.budongbudong.domain.property.realdeal;

import com.example.budongbudong.domain.property.realdeal.repository.RealDealRepository;
import com.example.budongbudong.domain.property.realdeal.document.RealDealDocument;
import com.example.budongbudong.domain.property.realdeal.enums.GeoStatus;
import com.example.budongbudong.domain.property.realdeal.service.DealCollectService;
import com.example.budongbudong.domain.property.realdeal.service.DealGeoCodingService;
import com.example.budongbudong.domain.property.realdeal.service.DealIndexService;
import com.example.budongbudong.domain.property.realdeal.service.DealPipelineService;
import com.example.budongbudong.domain.property.realdeal.service.DealSearchService;
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

    @Autowired
    DealSearchService searchService;

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
        long successCount = realDealRepository.countByGeoStatus(GeoStatus.SUCCESS);
        long failedCount = realDealRepository.countByGeoStatus(GeoStatus.FAILED);
        long retryCount = realDealRepository.countByGeoStatus(GeoStatus.RETRY);

        System.out.println("=== 파이프라인 결과 ===");
        System.out.println("총 수집: " + totalCount + "건");
        System.out.println("지오코딩 성공(SUCCESS): " + successCount + "건");
        System.out.println("지오코딩 실패(FAILED): " + failedCount + "건");
        System.out.println("재시도 대상(RETRY): " + retryCount + "건");

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

        long pendingBefore = realDealRepository.countByGeoStatus(GeoStatus.PENDING);
        System.out.println("지오코딩 전 PENDING: " + pendingBefore + "건");

        // when
        int geocoded = geoCodingService.geocodePending();

        // then
        long successCount = realDealRepository.countByGeoStatus(GeoStatus.SUCCESS);
        long failedCount = realDealRepository.countByGeoStatus(GeoStatus.FAILED);
        long retryCount = realDealRepository.countByGeoStatus(GeoStatus.RETRY);

        System.out.println("=== 지오코딩 결과 ===");
        System.out.println("성공(SUCCESS): " + successCount + "건");
        System.out.println("실패(FAILED): " + failedCount + "건");
        System.out.println("재시도(RETRY): " + retryCount + "건");

        assertThat(geocoded).isGreaterThan(0);
        assertThat(successCount).isGreaterThan(0);
    }

    @Test
    @DisplayName("3단계 : ES 인덱싱 테스트 (수집+지오코딩 후 실행)")
    void ES_인덱싱_테스트() {
        // given - 먼저 수집 + 지오코딩
        String lawdCd = "11110";
        YearMonth targetMonth = YearMonth.of(2024, 1);
        collectService.collect(lawdCd, targetMonth, targetMonth);
        geoCodingService.geocodePending();

        // when
        int indexed = indexService.indexAll();

        // then
        System.out.println("ES 인덱싱 완료: " + indexed + "건");

        assertThat(indexed).isGreaterThan(0);
    }

    /**
     * 260129사용자가 직접 좌표를 입력하는 상태
     *
     * todo
     * 1. 사용자가 주소입력 → 지오코딩으로 좌표 변환
     * 2. 해당 좌표로 주변 시세 검색
     * 3. 단위 n.n억원으로
     */
    @Test
    @DisplayName("4단계 : 반경 1km 내 주변 시세 검색 테스트")
    void 주변_시세_검색_테스트() {
        // given - 파이프라인 실행 (수집 → 지오코딩 → 인덱싱)
        String lawdCd = "11110";
        YearMonth targetMonth = YearMonth.of(2024, 1);
        pipelineService.runFullPipeline(List.of(lawdCd), targetMonth, targetMonth);

        // 종로구 광화문 근처 좌표
        double lat = 37.5759;
        double lon = 126.9769;

        // when - 반경 1km 내 검색
        List<RealDealDocument> results = searchService.findNearby(lat, lon, 1.0, 50);

        // then
        System.out.println("=== 주변 시세 검색 결과 (반경 1km) ===");
        System.out.println("검색 좌표: " + lat + ", " + lon);
        System.out.println("검색 결과: " + results.size() + "건");

        for (RealDealDocument doc : results) {
            System.out.printf("- %s | %s | %s | %.2f㎡ | %,d원%n",
                    doc.getPropertyName(),
                    doc.getAddress(),
                    doc.getPropertyType(),
                    doc.getExclusiveArea(),
                    doc.getDealAmount().longValue());
        }

        assertThat(results).isNotEmpty();
    }
}
