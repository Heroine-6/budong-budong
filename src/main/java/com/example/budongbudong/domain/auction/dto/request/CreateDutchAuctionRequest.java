package com.example.budongbudong.domain.auction.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CreateDutchAuctionRequest {

    @NotNull(message = "매물ID는 필수입니다.")
    private Long propertyId;

    @NotNull(message = "경매 시작일은 필수입니다.")
    @Future(message = "경매 시작일은 미래 날짜여야 합니다.")
    private LocalDateTime startedAt;

    @NotNull(message = "시작가는 필수입니다.")
    @Positive(message = "시작가는 0보다 커야 합니다.")
    private BigDecimal startPrice;

    @Positive(message = "하한가는 0보다 커야 합니다.")
    private BigDecimal endPrice;

    @NotNull(message = "감가율은 필수입니다.")
    @Positive(message = "감가율은 0보다 커야 합니다.")
    private int decreaseRate;
}