package com.example.budongbudong.domain.property.realdeal.dto;

import com.example.budongbudong.domain.property.enums.PropertyType;
import com.example.budongbudong.domain.property.realdeal.enums.DealSortType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RealDealSearchRequest {

    @Schema(description = "검색할 주소", example = "서울특별시 종로구 숭인동")
    private String address;

    @Schema(description = "위도 (좌표 검색 시)")
    private Double lat;

    @Schema(description = "경도 (좌표 검색 시)")
    private Double lon;

    @Schema(description = "검색 반경 (km)", example = "1.0", defaultValue = "1.0")
    private Double distanceKm = 1.0;

    @Schema(description = "조회 건수", example = "50", defaultValue = "50")
    private Integer size = 50;

    @Schema(description = "최소 전용면적 (m²)", example = "59.0")
    private BigDecimal minArea;

    @Schema(description = "최대 전용면적 (m²)", example = "84.0")
    private BigDecimal maxArea;

    @Schema(description = "최소 거래금액 (원)", example = "100000000")
    private BigDecimal minPrice;

    @Schema(description = "최대 거래금액 (원)", example = "500000000")
    private BigDecimal maxPrice;

    @Schema(description = "매물 유형", example = "APARTMENT")
    private PropertyType propertyType;

    @Schema(description = "정렬 기준", example = "DISTANCE", defaultValue = "DISTANCE")
    private DealSortType sortType = DealSortType.DISTANCE;
}
