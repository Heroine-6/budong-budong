package com.example.budongbudong.domain.property.realdeal;

import com.example.budongbudong.common.entity.RealDeal;
import com.example.budongbudong.domain.property.enums.PropertyType;
import com.example.budongbudong.domain.property.realdeal.client.KakaoGeoClient;
import com.example.budongbudong.domain.property.realdeal.client.KakaoGeoResponse;
import com.example.budongbudong.domain.property.realdeal.client.NaverGeoClient;
import com.example.budongbudong.domain.property.realdeal.client.NaverGeoResponse;
import com.example.budongbudong.domain.property.realdeal.enums.GeoStatus;
import com.example.budongbudong.domain.property.realdeal.repository.RealDealRepository;
import com.example.budongbudong.domain.property.realdeal.service.DealGeoCodingService;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 지오코딩 실패 시나리오 테스트
 * - 주소 못 찾음 → FAILED (영구 실패)
 * - API 오류 → RETRY (재시도 대상)
 */
@SpringBootTest
class DealGeoCodingFailureTest {

    @Autowired
    private DealGeoCodingService geoCodingService;

    @Autowired
    private RealDealRepository realDealRepository;

    @MockitoBean
    private NaverGeoClient naverGeoClient;

    @MockitoBean
    private KakaoGeoClient kakaoGeoClient;

    @BeforeEach
    void setUp() {
        realDealRepository.deleteAll();
    }

    @Test
    @DisplayName("주소를 찾을 수 없으면 FAILED 상태가 된다")
    void 주소_못찾음_FAILED() {
        // given - 잘못된 주소로 RealDeal 생성
        RealDeal deal = createTestDeal("존재하지않는주소 123-456");
        realDealRepository.save(deal);

        // 네이버/카카오 모두 빈 결과 반환 (주소 못 찾음)
        when(naverGeoClient.geocode(anyString()))
                .thenReturn(new NaverGeoResponse("OK", new NaverGeoResponse.Meta(0), List.of()));
        when(kakaoGeoClient.geocode(anyString()))
                .thenReturn(new KakaoGeoResponse(List.of()));

        // when
        geoCodingService.geocodePending();

        // then
        RealDeal result = realDealRepository.findById(deal.getId()).orElseThrow();

        System.out.println("=== 주소 못 찾음 테스트 ===");
        System.out.println("주소: " + result.getAddress());
        System.out.println("상태: " + result.getGeoStatus());
        System.out.println("재시도 횟수: " + result.getRetryCount());

        assertThat(result.getGeoStatus()).isEqualTo(GeoStatus.FAILED);
        assertThat(result.getLatitude()).isNull();
    }

    @Test
    @DisplayName("API 오류(5xx) 발생 시 RETRY 상태가 된다")
    void API_서버오류_RETRY() {
        // given
        RealDeal deal = createTestDeal("서울특별시 종로구 숭인동 766");
        realDealRepository.save(deal);

        // 네이버/카카오 모두 500 에러
        when(naverGeoClient.geocode(anyString()))
                .thenThrow(createFeignException(500, "Internal Server Error"));
        when(kakaoGeoClient.geocode(anyString()))
                .thenThrow(createFeignException(500, "Internal Server Error"));

        // when
        geoCodingService.geocodePending();

        // then
        RealDeal result = realDealRepository.findById(deal.getId()).orElseThrow();

        System.out.println("=== API 서버 오류 테스트 ===");
        System.out.println("주소: " + result.getAddress());
        System.out.println("상태: " + result.getGeoStatus());
        System.out.println("재시도 횟수: " + result.getRetryCount());

        assertThat(result.getGeoStatus()).isEqualTo(GeoStatus.RETRY);
        assertThat(result.getRetryCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("API 권한 오류(401/403) 발생 시 RETRY 상태가 된다")
    void API_권한오류_RETRY() {
        // given
        RealDeal deal = createTestDeal("서울특별시 강남구 대치동 316");
        realDealRepository.save(deal);

        // 네이버 401, 카카오 403
        when(naverGeoClient.geocode(anyString()))
                .thenThrow(createFeignException(401, "Unauthorized"));
        when(kakaoGeoClient.geocode(anyString()))
                .thenThrow(createFeignException(403, "Forbidden"));

        // when
        geoCodingService.geocodePending();

        // then
        RealDeal result = realDealRepository.findById(deal.getId()).orElseThrow();

        System.out.println("=== API 권한 오류 테스트 ===");
        System.out.println("주소: " + result.getAddress());
        System.out.println("상태: " + result.getGeoStatus());
        System.out.println("재시도 횟수: " + result.getRetryCount());

        assertThat(result.getGeoStatus()).isEqualTo(GeoStatus.RETRY);
        assertThat(result.getRetryCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Rate Limit(429) 발생 시 RETRY 상태가 된다")
    void API_레이트리밋_RETRY() {
        // given
        RealDeal deal = createTestDeal("서울특별시 서초구 반포동 1-1");
        realDealRepository.save(deal);

        // 429 Too Many Requests
        when(naverGeoClient.geocode(anyString()))
                .thenThrow(createFeignException(429, "Too Many Requests"));
        when(kakaoGeoClient.geocode(anyString()))
                .thenThrow(createFeignException(429, "Too Many Requests"));

        // when
        geoCodingService.geocodePending();

        // then
        RealDeal result = realDealRepository.findById(deal.getId()).orElseThrow();

        System.out.println("=== Rate Limit 테스트 ===");
        System.out.println("주소: " + result.getAddress());
        System.out.println("상태: " + result.getGeoStatus());
        System.out.println("재시도 횟수: " + result.getRetryCount());

        assertThat(result.getGeoStatus()).isEqualTo(GeoStatus.RETRY);
    }

    @Test
    @DisplayName("재시도 3회 초과 시 FAILED 상태로 변경된다")
    void 재시도_초과_FAILED() {
        // given - 이미 2회 재시도한 상태
        RealDeal deal = createTestDeal("서울특별시 종로구 청운동 1");
        realDealRepository.save(deal);

        // API 계속 실패
        when(naverGeoClient.geocode(anyString()))
                .thenThrow(createFeignException(500, "Server Error"));
        when(kakaoGeoClient.geocode(anyString()))
                .thenThrow(createFeignException(500, "Server Error"));

        // when - 3회 재시도
        for (int i = 0; i < 3; i++) {
            if (i == 0) {
                geoCodingService.geocodePending();
            } else {
                geoCodingService.geocodeRetry();
            }
        }

        // then
        RealDeal result = realDealRepository.findById(deal.getId()).orElseThrow();

        System.out.println("=== 재시도 초과 테스트 ===");
        System.out.println("주소: " + result.getAddress());
        System.out.println("상태: " + result.getGeoStatus());
        System.out.println("재시도 횟수: " + result.getRetryCount());

        assertThat(result.getGeoStatus()).isEqualTo(GeoStatus.FAILED);
        assertThat(result.getRetryCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("네이버 실패 후 카카오 성공 시 SUCCESS 상태가 된다")
    void 네이버실패_카카오성공_SUCCESS() {
        // given
        RealDeal deal = createTestDeal("서울특별시 마포구 상암동 1600");
        realDealRepository.save(deal);

        // 네이버 실패, 카카오 성공
        when(naverGeoClient.geocode(anyString()))
                .thenThrow(createFeignException(500, "Server Error"));
        when(kakaoGeoClient.geocode(anyString()))
                .thenReturn(new KakaoGeoResponse(List.of(
                        new KakaoGeoResponse.Document("서울 마포구 상암동 1600", "126.8891", "37.5779")
                )));

        // when
        geoCodingService.geocodePending();

        // then
        RealDeal result = realDealRepository.findById(deal.getId()).orElseThrow();

        System.out.println("=== 네이버 실패 카카오 성공 테스트 ===");
        System.out.println("주소: " + result.getAddress());
        System.out.println("상태: " + result.getGeoStatus());
        System.out.println("위도: " + result.getLatitude());
        System.out.println("경도: " + result.getLongitude());

        assertThat(result.getGeoStatus()).isEqualTo(GeoStatus.SUCCESS);
        assertThat(result.getLatitude()).isNotNull();
        assertThat(result.getLongitude()).isNotNull();
    }

    private RealDeal createTestDeal(String address) {
        return RealDeal.builder()
                .propertyName("테스트매물")
                .address(address)
                .dealAmount(new BigDecimal("500000000"))
                .exclusiveArea(new BigDecimal("84.32"))
                .floor(10)
                .builtYear(2020)
                .dealDate(LocalDate.of(2024, 1, 15))
                .propertyType(PropertyType.APARTMENT)
                .lawdCd("11110")
                .build();
    }

    private FeignException createFeignException(int status, String reason) {
        Request request = Request.create(
                Request.HttpMethod.GET,
                "http://test",
                Collections.emptyMap(),
                null,
                new RequestTemplate()
        );
        return FeignException.errorStatus("test", feign.Response.builder()
                .status(status)
                .reason(reason)
                .request(request)
                .headers(Map.of())
                .build());
    }
}
