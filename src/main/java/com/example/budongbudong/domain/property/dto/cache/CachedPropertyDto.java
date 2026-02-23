package com.example.budongbudong.domain.property.dto.cache;

import com.example.budongbudong.domain.property.dto.response.ReadAllPropertyResponse;
import com.example.budongbudong.domain.property.enums.PropertyType;
import lombok.*;

import java.math.BigDecimal;

/**
 * Cache 에서 사용할 단건 매물용 Dto
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CachedPropertyDto {

    private Long id;
    private String name;
    private String address;
    private PropertyType type;
    private String description;
    private BigDecimal supplyArea;
    private BigDecimal privateArea;
    private String thumbnailUrl;

    private CachedAuctionDto auction; // nullable

    public static CachedPropertyDto from(ReadAllPropertyResponse response) {
        return new CachedPropertyDto(
                response.getId(),
                response.getName(),
                response.getAddress(),
                response.getType(),
                response.getDescription(),
                response.getSupplyArea(),
                response.getPrivateArea(),
                response.getThumbnailImage(),
                CachedAuctionDto.from(response.getAuction())
        );
    }

    public ReadAllPropertyResponse toResponse() {
        return ReadAllPropertyResponse.fromCached(
                id,
                name,
                address,
                type,
                description,
                supplyArea,
                privateArea,
                thumbnailUrl,
                auction != null ? auction.toResponse() : null
        );
    }
}

