package com.example.budongbudong.domain.property.realdeal;

import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.property.realdeal.document.RealDealDocument;
import com.example.budongbudong.domain.property.realdeal.dto.RealDealSearchRequest;
import com.example.budongbudong.domain.property.realdeal.dto.RealDealSearchResponse;
import com.example.budongbudong.domain.property.realdeal.service.DealSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
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
    public ResponseEntity<GlobalResponse<RealDealSearchResponse>> searchNearby(
            @ParameterObject @ModelAttribute RealDealSearchRequest request
    ) {
        if (request.getMinArea() != null && request.getMaxArea() != null
                && request.getMinArea().compareTo(request.getMaxArea()) > 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        if (request.getMinPrice() != null && request.getMaxPrice() != null
                && request.getMinPrice().compareTo(request.getMaxPrice()) > 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        SearchHits<RealDealDocument> searchHits;

        // 좌표가 있으면 좌표 우선 (반경 검색)
        if (request.getLat() != null && request.getLon() != null) {
            searchHits = dealSearchService.findNearby(
                    request.getLat(), request.getLon(), request.getDistanceKm(), request.getSize(),
                    request.getMinArea(), request.getMaxArea(), request.getMinPrice(), request.getMaxPrice(),
                    request.getPropertyType(), request.getSortType()
            );
        }
        // 주소가 있으면 지오코딩 → 반경 검색
        else if (request.getAddress() != null && !request.getAddress().isBlank()) {
            searchHits = dealSearchService.findByAddress(
                    request.getAddress(), request.getDistanceKm(), request.getSize(),
                    request.getMinArea(), request.getMaxArea(), request.getMinPrice(), request.getMaxPrice(),
                    request.getPropertyType(), request.getSortType()
            );
        }
        // 둘 다 없으면 에러
        else {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        long totalCount = searchHits.getTotalHits();
        List<RealDealDocument> deals = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();

        return GlobalResponse.ok(RealDealSearchResponse.of(totalCount, deals));
    }
}
