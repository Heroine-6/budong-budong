package com.example.budongbudong.domain.property.dto.request;

import com.example.budongbudong.domain.property.enums.PropertyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PropertyLookupRequest(
        @NotNull(message = "매물 유형은 필수입니다.")
        PropertyType type,

        @NotBlank(message = "주소는 필수입니다.")
        String address,

        @NotBlank(message = "계약연월은 필수입니다.")
        String dealYmd,

        @NotNull(message = "층수는 필수입니다.")
        Integer floor
) {
}
