package com.example.budongbudong.domain.property.dto.request;

import com.example.budongbudong.domain.property.entity.Property;
import com.example.budongbudong.domain.property.enums.PropertyType;
import com.example.budongbudong.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;

public record CreatePropertyRequestDTO(
        @NotBlank(message = "매물명은 필수입니다.")
        String name,

        @NotBlank(message = "주소는 필수입니다.")
        String address,

        @NotNull(message = "가격은 필수입니다.")
        @Positive(message = "가격은 양수여야 합니다.")
        Long price,

        @NotNull(message = "층수는 필수입니다.")
        Integer floor,

        @NotNull(message = "총 층수는 필수입니다.")
        Integer totalFloor,

        @NotNull(message = "방 개수는 필수입니다.")
        Integer roomCount,

        @NotNull(message = "전용면적은 필수입니다.")
        BigDecimal privateArea,

        @NotNull(message = "공급면적은 필수입니다.")
        BigDecimal supplyArea,

        @NotBlank(message = "건축년도는 필수입니다.")
        String builtYear,

        @NotNull(message = "입주가능일은 필수입니다.")
        LocalDate migrateDate,

        String description,

        @NotNull(message = "매물 유형은 필수입니다.")
        PropertyType type
) {
    public Property toEntity(User user) {
        return Property.builder()
                .name(name)
                .address(address)
                .price(price)
                .floor(floor)
                .totalFloor(totalFloor)
                .roomCount(roomCount)
                .privateArea(privateArea)
                .supplyArea(supplyArea)
                .builtYear(Year.of(Integer.parseInt(builtYear)))
                .migrateDate(migrateDate)
                .description(description)
                .type(type)
                .user(user)
                .build();
    }
}
