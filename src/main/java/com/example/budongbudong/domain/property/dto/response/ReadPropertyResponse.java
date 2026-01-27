package com.example.budongbudong.domain.property.dto.response;

import com.example.budongbudong.common.entity.Property;
import com.example.budongbudong.common.entity.PropertyImage;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.property.enums.PropertyType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ReadPropertyResponse {

    private final String name;
    private final String address;
    private final BigDecimal price;
    private final int floor;
    private final int totalFloor;
    private final int roomCount;
    private final BigDecimal privateArea;
    private final BigDecimal supplyArea;
    private final Year builtYear;
    private final LocalDate migratedDate;
    private final String description;
    private final PropertyType type;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final AuctionStatus status;
    private final List<String> images;

    public static ReadPropertyResponse from(Property property, AuctionStatus auctionStatus) {
        return new ReadPropertyResponse(
                property.getName(),
                property.getAddress(),
                property.getPrice(),
                property.getFloor(),
                property.getTotalFloor(),
                property.getRoomCount(),
                property.getPrivateArea(),
                property.getSupplyArea(),
                property.getBuiltYear(),
                property.getMigrateDate(),
                property.getDescription(),
                property.getType(),
                auctionStatus,
                property.getPropertyImageList().stream()
                        .map(PropertyImage::getImageUrl)
                        .toList()
        );
    }
}
