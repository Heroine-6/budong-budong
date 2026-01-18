package com.example.budongbudong.domain.property.dto.response;

import com.example.budongbudong.domain.auction.dto.response.AuctionResponse;
import com.example.budongbudong.domain.property.dto.ReadAllPropertyDto;
import com.example.budongbudong.domain.property.entity.Property;
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

    public static ReadAllPropertyResponse from(Property property, AuctionResponse auction) {

        // thumbnailImage 없음
        if (property.getPropertyImageList() == null ||
                property.getPropertyImageList().isEmpty()) {

            return new ReadAllPropertyResponse(
                    property.getId(),
                    property.getName(),
                    property.getAddress(),
                    property.getType(),
                    property.getDescription(),
                    property.getSupplyArea(),
                    property.getPrivateArea(),
                    null,
                    auction
            );
        }
        String thumbnailImage =
                property.getPropertyImageList()
                        .get(0)
                        .getImageUrl();

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

    /**
     *  QueryDsl 조회 결과 기반 응답 DTO
     * */
    public static ReadAllPropertyResponse of (
            ReadAllPropertyDto dto,
            AuctionResponse auction
    ) {
        return new ReadAllPropertyResponse(
                dto.getPropertyId(),
                dto.getName(),
                dto.getAddress(),
                dto.getType(),
                dto.getDescription(),
                dto.getSupplyArea(),
                dto.getPrivateArea(),
                dto.getThumbnailUrl(),
                auction
        );
    }


}
