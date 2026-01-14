package com.example.budongbudong.domain.property.dto.response;

import com.example.budongbudong.domain.auction.dto.AuctionResponse;
import com.example.budongbudong.domain.property.entity.Property;
import com.example.budongbudong.domain.property.enums.PropertyType;
import com.example.budongbudong.domain.propertyimage.dto.PropertyImageResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class ReadAllPropertyResponse {

    private final Long id;
    private final String name;
    private final String address;
    private final PropertyType type;
    private final String description;
    private final BigDecimal supplyArea;
    private final BigDecimal privateArea;
    private final List<PropertyImageResponse> images;
    private final AuctionResponse auction;

    public static ReadAllPropertyResponse from(Property property) {

        List<PropertyImageResponse> images = property.getPropertyImageList() == null
                ? List.of()
                :property.getPropertyImageList().stream()
                .map(PropertyImageResponse::from)
                .toList();

        AuctionResponse auction = property.getAuction() == null
                ? null
                : AuctionResponse.from(property.getAuction());
        return  new ReadAllPropertyResponse(
                property.getId(),
                property.getName(),
                property.getAddress(),
                property.getType(),
                property.getDescription(),
                property.getSupplyArea(),
                property.getPrivateArea(),
                images,
                auction
        );
    }
}
