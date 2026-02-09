package com.example.budongbudong.domain.property.realdeal.dto;

import com.example.budongbudong.domain.property.realdeal.document.RealDealDocument;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Builder
public class MarketCompareResponse {

    // 경매 기본 정보
    private final BigDecimal startPrice;
    private final BigDecimal highestBidPrice;
    private final BigDecimal privateArea;

    // 평단가 비교
    private final BigDecimal startPricePerArea;
    private final BigDecimal highestBidPricePerArea;
    private final BigDecimal inputPricePerArea;

    // 주변 시세 통계
    private final long totalCount;
    private final BigDecimal medianPricePerArea;
    private final BigDecimal minDealAmount;
    private final BigDecimal maxDealAmount;

    // 시세 대비 비율 (%) — 중앙값 기준
    private final double startPriceRatio;
    private final double highestBidPriceRatio;
    private final double inputPriceRatio;

    // 비교 대상 실거래 목록
    private final List<RealDealDocument> deals;

    public static MarketCompareResponse of(BigDecimal startPrice, BigDecimal highestBidPrice,
                                           BigDecimal privateArea, BigDecimal inputPrice,
                                           long totalCount, List<RealDealDocument> deals) {
        BigDecimal startPpa = calcPricePerArea(startPrice, privateArea);
        BigDecimal bidPpa = calcPricePerArea(highestBidPrice, privateArea);
        BigDecimal inputPpa = inputPrice != null ? calcPricePerArea(inputPrice, privateArea) : null;

        if (deals.isEmpty()) {
            return MarketCompareResponse.builder()
                    .startPrice(startPrice)
                    .highestBidPrice(highestBidPrice)
                    .privateArea(privateArea)
                    .startPricePerArea(startPpa)
                    .highestBidPricePerArea(bidPpa)
                    .inputPricePerArea(inputPpa)
                    .totalCount(totalCount)
                    .medianPricePerArea(BigDecimal.ZERO)
                    .minDealAmount(BigDecimal.ZERO)
                    .maxDealAmount(BigDecimal.ZERO)
                    .startPriceRatio(0)
                    .highestBidPriceRatio(0)
                    .inputPriceRatio(0)
                    .deals(deals)
                    .build();
        }

        BigDecimal min = null;
        BigDecimal max = null;
        List<BigDecimal> pricePerAreas = new ArrayList<>();

        for (RealDealDocument deal : deals) {
            BigDecimal amount = deal.getDealAmount();
            if (amount == null) continue;

            if (min == null || amount.compareTo(min) < 0) min = amount;
            if (max == null || amount.compareTo(max) > 0) max = amount;

            if (deal.getExclusiveArea() != null && deal.getExclusiveArea().compareTo(BigDecimal.ZERO) > 0) {
                pricePerAreas.add(amount.divide(deal.getExclusiveArea(), 0, RoundingMode.HALF_UP));
            }
        }

        BigDecimal medianPpa = calcMedian(pricePerAreas);

        return MarketCompareResponse.builder()
                .startPrice(startPrice)
                .highestBidPrice(highestBidPrice)
                .privateArea(privateArea)
                .startPricePerArea(startPpa)
                .highestBidPricePerArea(bidPpa)
                .inputPricePerArea(inputPpa)
                .totalCount(totalCount)
                .medianPricePerArea(medianPpa)
                .minDealAmount(min)
                .maxDealAmount(max)
                .startPriceRatio(calcRatio(startPpa, medianPpa))
                .highestBidPriceRatio(calcRatio(bidPpa, medianPpa))
                .inputPriceRatio(inputPpa != null ? calcRatio(inputPpa, medianPpa) : 0)
                .deals(deals)
                .build();
    }

    private static BigDecimal calcPricePerArea(BigDecimal price, BigDecimal area) {
        if (price == null || area == null || area.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return price.divide(area, 0, RoundingMode.HALF_UP);
    }

    private static BigDecimal calcMedian(List<BigDecimal> values) {
        if (values.isEmpty()) return BigDecimal.ZERO;

        List<BigDecimal> sorted = new ArrayList<>(values);
        Collections.sort(sorted);

        int size = sorted.size();
        if (size % 2 == 1) {
            return sorted.get(size / 2);
        }
        return sorted.get(size / 2 - 1)
                .add(sorted.get(size / 2))
                .divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP);
    }

    private static double calcRatio(BigDecimal ppa, BigDecimal medianPpa) {
        if (ppa.compareTo(BigDecimal.ZERO) <= 0 || medianPpa.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return ppa.multiply(BigDecimal.valueOf(100))
                .divide(medianPpa, 1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
