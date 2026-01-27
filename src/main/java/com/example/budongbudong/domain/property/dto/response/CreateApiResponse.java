package com.example.budongbudong.domain.property.dto.response;

import java.math.BigDecimal;
import java.time.Year;

public record CreateApiResponse(
        String name,
        String address,
        BigDecimal price,
        BigDecimal privateArea,
        Year builtYear
) {
}
