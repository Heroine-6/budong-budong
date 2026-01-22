package com.example.budongbudong.fixture;

import com.example.budongbudong.common.entity.Property;
import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.domain.property.enums.PropertyType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;

public class PropertyFixture {

    public static Property property(User user) {
        return Property.builder()
                .name("테스트 매물")
                .address("서울시 테스트구 테스트동 123-45")
                .floor(3)
                .totalFloor(10)
                .roomCount(2)
                .type(PropertyType.APARTMENT)
                .builtYear(Year.of(2015))
                .description("테스트 매물 설명")
                .price(300_000_000L)
                .migrateDate(LocalDate.now().plusMonths(1))
                .supplyArea(BigDecimal.valueOf(84.32))
                .privateArea(BigDecimal.valueOf(59.87))
                .user(user)
                .build();
    }

}
