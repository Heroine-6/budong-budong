package com.example.budongbudong.domain.property.dto.response;

import com.example.budongbudong.common.entity.Property;
import com.example.budongbudong.domain.property.enums.PropertyType;
import com.example.budongbudong.domain.propertyimage.dto.PropertyImageResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class PropertyResponse {
    private final Long id;
    private final String name;
    private final String address;
    private final int floor;
    private final int totalFloor;
    private final int roomCount;
    private final PropertyType type;
    private final Year builtYear;
    private final String description;
    private final Long price;
    private final LocalDate migrateDate;
    private final BigDecimal supplyArea;
    private final BigDecimal privateArea;
    private final List<PropertyImageResponse> images;

    public static PropertyResponse from(Property property) {

        List<PropertyImageResponse> images = property.getPropertyImageList() == null
                ? List.of()
                : property.getPropertyImageList().stream()
                .map(PropertyImageResponse::from)
                .toList();

        return new PropertyResponse(
                property.getId(),
                property.getName(),
                property.getAddress(),
                property.getFloor(),
                property.getTotalFloor(),
                property.getRoomCount(),
                property.getType(),
                property.getBuiltYear(),
                property.getDescription(),
                property.getPrice(),
                property.getMigrateDate(),
                property.getSupplyArea(),
                property.getPrivateArea(),
                images
        );
    }
}
