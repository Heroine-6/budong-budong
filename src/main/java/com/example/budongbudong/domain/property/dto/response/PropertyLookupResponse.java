package com.example.budongbudong.domain.property.dto.response;

import java.math.BigDecimal;
import java.time.Year;

public record PropertyLookupResponse(
        String name,
        BigDecimal price,
        BigDecimal privateArea,
        Year builtYear
) {
    public static PropertyLookupResponse from(CreateApiResponse api) {
        return new PropertyLookupResponse(
                api.name(),
                api.price(),
                api.privateArea(),
                api.builtYear()
        );
    }
}
