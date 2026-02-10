package com.example.budongbudong.domain.property.realdeal;

import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.property.realdeal.dto.MarketCompareResponse;
import com.example.budongbudong.domain.property.realdeal.dto.RealDealSearchRequest;
import com.example.budongbudong.domain.property.realdeal.dto.RealDealSearchResponse;
import com.example.budongbudong.domain.property.realdeal.service.DealSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 실거래가 검색 API
 * - 주소 또는 좌표 기준으로 주변 시세 조회
 */
@Tag(name = "실거래가", description = "실거래가 검색 API")
@RestController
@RequestMapping("/api/v2/real-deals")
@RequiredArgsConstructor
public class RealDealController {

    private final DealSearchService dealSearchService;

    /**
     * 주변 시세 검색
     * - address 입력 시: 지오코딩 → 좌표 기준 반경 검색
     * - lat/lon 입력 시: 좌표 기준 반경 검색
     * - 둘 다 입력 시: lat/lon 우선
     */
    @Operation(summary = "주변 시세 검색", description = "주소 또는 좌표 기준으로 반경 내 실거래가를 조회합니다.")
    @GetMapping("/nearby")
    public ResponseEntity<GlobalResponse<RealDealSearchResponse>> searchNearby(
            @ParameterObject @ModelAttribute RealDealSearchRequest request
    ) {
        RealDealSearchResponse response = dealSearchService.searchNearby(request);
        return GlobalResponse.ok(response);
    }

    @Operation(summary = "입찰가 주변시세 비교", description = "경매 시작가·최고입찰가·희망입찰가 각각의 m²당 평단가를 주변 실거래 중앙값 평단가와 비교하여 시세 대비 비율(%)을 반환합니다.")
    @GetMapping("/compare/{auctionId}")
    public ResponseEntity<GlobalResponse<MarketCompareResponse>> compareWithMarket(
            @PathVariable Long auctionId,

            @Parameter(description = "검색 반경 (km)", example = "1.0")
            @RequestParam(defaultValue = "1.0") double distanceKm,

            @Parameter(description = "조회 건수", example = "50")
            @RequestParam(defaultValue = "50") int size,

            @Parameter(description = "희망 입찰가, 만원 단위 (선택)", example = "50000")
            @RequestParam(required = false) BigDecimal inputPrice
    ) {
        MarketCompareResponse response = dealSearchService.compareWithAuction(auctionId, distanceKm, size, inputPrice);
        return GlobalResponse.ok(response);
    }
}
