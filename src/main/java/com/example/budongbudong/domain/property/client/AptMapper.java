package com.example.budongbudong.domain.property.client;

import com.example.budongbudong.domain.property.dto.response.CreateApiResponse;

import java.math.BigDecimal;

public class AptMapper {

    // 필요한 형태의 값으로 변환
    public static CreateApiResponse toCreateApiResponse(AptItem item, String requestAddress) {
        return new CreateApiResponse(
                item.getName(),
                requestAddress,
                parseDealAmount(item.dealAmount()),
                parseArea(item.excluUseAr()),
                item.buildYear()
        );
    }

    // 매매가를 원 단위로 변환 (API는 만원 단위로 제공)
    private static BigDecimal parseDealAmount(String dealAmount) {
        if (dealAmount == null) return null;
        String normalized = dealAmount.replace(",", "").replace(" ", "");
        if (normalized.isBlank()) return null;
        return new BigDecimal(normalized).multiply(BigDecimal.valueOf(10000));
    }

    // 전용 면적을 BigDecimal로 변환
    private static BigDecimal parseArea(String area) {
        if (area == null || area.isBlank()) return null;
        return new BigDecimal(area.trim());
    }
}
