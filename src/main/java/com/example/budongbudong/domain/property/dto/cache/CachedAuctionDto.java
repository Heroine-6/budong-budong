package com.example.budongbudong.domain.property.dto.cache;

import com.example.budongbudong.domain.auction.dto.response.AuctionResponse;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import lombok.*;

import java.math.BigDecimal;

/**
 * Cache 경매 요약 Dto
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CachedAuctionDto {

    private Long auctionId;
    private BigDecimal startPrice;
    private AuctionStatus status;

    public static CachedAuctionDto from(AuctionResponse response) {
        if (response == null) return null;

        return new CachedAuctionDto(
                response.getId(),
                response.getStartPrice(),
                response.getStatus()

        );
    }

    public AuctionResponse toResponse() {
        return new AuctionResponse(
                auctionId,
                startPrice,
                status
        );
    }
}

