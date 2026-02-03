package com.example.budongbudong.domain.property.realdeal;

import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.property.realdeal.document.RealDealDocument;
import com.example.budongbudong.domain.property.realdeal.service.DealSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<GlobalResponse<List<RealDealDocument>>> searchNearby(
            @Parameter(description = "검색할 주소", example = "서울특별시 종로구 숭인동")
            @RequestParam(required = false) String address,

            @Parameter(description = "위도 (좌표 검색 시)")
            @RequestParam(required = false) Double lat,

            @Parameter(description = "경도 (좌표 검색 시)")
            @RequestParam(required = false) Double lon,

            @Parameter(description = "검색 반경 (km)", example = "1.0")
            @RequestParam(defaultValue = "1.0") double distanceKm,

            @Parameter(description = "조회 건수", example = "50")
            @RequestParam(defaultValue = "50") int size
    ) {
        List<RealDealDocument> results;

        // 좌표가 있으면 좌표 우선 (반경 검색)
        if (lat != null && lon != null) {
            results = dealSearchService.findNearby(lat, lon, distanceKm, size);
        }
        // 주소가 있으면 지오코딩 → 반경 검색
        else if (address != null && !address.isBlank()) {
            results = dealSearchService.findByAddress(address, distanceKm, size);
        }
        // 둘 다 없으면 에러
        else {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        return GlobalResponse.ok(results);
    }
}
