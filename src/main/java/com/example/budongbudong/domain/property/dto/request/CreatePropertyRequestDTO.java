package com.example.budongbudong.domain.property.dto.request;

import com.example.budongbudong.domain.property.entity.Property;
import com.example.budongbudong.domain.property.enums.PropertyType;
import com.example.budongbudong.domain.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreatePropertyRequestDTO(

        @NotBlank(message = "지역번호는 필수입니다.")
        String lawdCd,

        @NotBlank(message = "계약연월은 필수입니다.")
        String dealYmd,

        @NotBlank(message = "주소는 필수입니다.")
        String address,

        @NotNull(message = "층수는 필수입니다.")
        Integer floor,

        @NotNull(message = "총 층수는 필수입니다.")
        Integer totalFloor,

        @NotNull(message = "방 개수는 필수입니다.")
        Integer roomCount,

        @NotNull(message = "공급면적은 필수입니다.")
        BigDecimal supplyArea,

        @NotNull(message = "입주가능일은 필수입니다.")
        LocalDate migrateDate,

        String description,

        @NotNull(message = "매물 유형은 필수입니다.")
        PropertyType type
) {
    public Property toEntity(User user) {
        return Property.builder()
                .address(address)
                .floor(floor)
                .totalFloor(totalFloor)
                .roomCount(roomCount)
                .supplyArea(supplyArea)
                .migrateDate(migrateDate)
                .description(description)
                .type(type)
                .user(user)
                .build();
    }
}
