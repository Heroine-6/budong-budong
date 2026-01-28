package com.example.budongbudong.domain.auction.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class CreateAuctionRequest {

    @NotNull(message = "매물ID는 필수입니다.")
    private Long propertyId;

    @NotNull(message = "시작가는 필수입니다.")
    @Positive(message = "시작가는 0보다 커야 합니다.")
    private BigDecimal startPrice;

    @NotNull(message = "경매 시작 시간은 필수입니다.")
    private LocalDateTime startedAt;

    @NotNull(message = "경매 종료 시간은 필수입니다.")
    private LocalDateTime endedAt;

}