package com.example.budongbudong.common.api;

import com.example.budongbudong.domain.property.dto.response.CreateApiResponse;

import java.math.BigDecimal;

public class AptMapper {

    public static CreateApiResponse toCreateApiResponse(AptItem item, String requestAddress) {
        return new CreateApiResponse(
                item.aptNm(),
                requestAddress,
                parseDealAmount(item.dealAmount()),
                parseArea(item.excluUseAr()),
                item.buildYear()
        );
    }

    private static Long parseDealAmount(String dealAmount) {
        if (dealAmount == null) return null;
        String normalized = dealAmount.replace(",", "").replace(" ", "");
        if (normalized.isBlank()) return null;
        return Long.parseLong(normalized);
    }

    private static BigDecimal parseArea(String area) {
        if (area == null || area.isBlank()) return null;
        return new BigDecimal(area.trim());
    }
}
