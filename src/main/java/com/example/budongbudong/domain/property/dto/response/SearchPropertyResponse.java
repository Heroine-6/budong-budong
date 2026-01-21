package com.example.budongbudong.domain.property.dto.response;

import com.example.budongbudong.domain.auction.dto.response.AuctionResponse;
import com.example.budongbudong.domain.property.document.PropertySearchDocument;
import com.example.budongbudong.domain.property.enums.PropertyType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public class SearchPropertyResponse {

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

    public static SearchPropertyResponse from(PropertySearchDocument document) {

        return new SearchPropertyResponse(
                document.getId(),
                document.getName(),
                document.getAddress(),
                document.getType(),
                document.getDescription(),
                document.getSupplyArea(),
                document.getPrivateArea(),
                document.getThumbnailImage(),
                document.getAuction() != null
                        ? AuctionResponse.from(document.getAuction())
                        : null
        );
    }
}
