package com.example.budongbudong.domain.property.realdeal.dto;

import com.example.budongbudong.domain.property.enums.PropertyType;
import com.example.budongbudong.domain.property.realdeal.enums.DealSortType;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RealDealSearchRequest {

    @Parameter(description = "검색할 주소", example = "서울특별시 종로구 숭인동")
    private String address;

    @Parameter(description = "위도 (좌표 검색 시)")
    private Double lat;

    @Parameter(description = "경도 (좌표 검색 시)")
    private Double lon;

    @Parameter(description = "검색 반경 (km)", example = "1.0")
    private Double distanceKm = 1.0;

    @Parameter(description = "조회 건수", example = "50")
    private Integer size = 50;

    @Parameter(description = "최소 전용면적 (m²)", example = "59.0")
    private BigDecimal minArea;

    @Parameter(description = "최대 전용면적 (m²)", example = "84.0")
    private BigDecimal maxArea;

    @Parameter(description = "최소 거래금액 (원)", example = "100000000")
    private BigDecimal minPrice;

    @Parameter(description = "최대 거래금액 (원)", example = "500000000")
    private BigDecimal maxPrice;

    @Parameter(description = "매물 유형 (APARTMENT, OFFICETEL, VILLA)")
    private PropertyType propertyType;

    @Parameter(description = "정렬 기준 (DISTANCE, PRICE_PER_AREA_ASC, PRICE_PER_AREA_DESC)")
    private DealSortType sortType = DealSortType.DISTANCE;
}
