package com.example.budongbudong.domain.property.dto.cache;

import com.example.budongbudong.domain.auction.dto.response.AuctionResponse;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cache 경매 요약 Dto
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CachedAuctionDto {

    private Long auctionId;
    private BigDecimal startPrice;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    public static CachedAuctionDto from(AuctionResponse response) {
        if (response == null) return null;

        return new CachedAuctionDto(
                response.getId(),
                response.getStartPrice(),
                response.getStatus().name(),
                response.getStartedAt(),
                response.getEndedAt()
        );
    }

    public AuctionResponse toResponse() {
        return new AuctionResponse(
                auctionId,
                startPrice,
                AuctionStatus.valueOf(status),
                startedAt,
                endedAt
        );
    }
}

