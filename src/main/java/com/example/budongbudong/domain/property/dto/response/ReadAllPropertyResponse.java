package com.example.budongbudong.domain.property.dto.response;

import com.example.budongbudong.common.entity.Property;
import com.example.budongbudong.domain.auction.dto.response.AuctionResponse;
import com.example.budongbudong.domain.property.enums.PropertyType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

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
    private final String thumbnailImage;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final AuctionResponse auction;

    public static ReadAllPropertyResponse from(Property property, AuctionResponse auction, String thumbnailImage) {

        return  new ReadAllPropertyResponse(
                property.getId(),
                property.getName(),
                property.getAddress(),
                property.getType(),
                property.getDescription(),
                property.getSupplyArea(),
                property.getPrivateArea(),
                thumbnailImage,
                auction
        );
    }

    public static ReadAllPropertyResponse fromCached(
            Long id,
            String name,
            String address,
            PropertyType type,
            String description,
            BigDecimal supplyArea,
            BigDecimal privateArea,
            String thumbnailImage,
            AuctionResponse auction
    ) {
        return new ReadAllPropertyResponse(
                id,
                name,
                address,
                type,
                description,
                supplyArea,
                privateArea,
                thumbnailImage,
                auction
        );
    }
}
