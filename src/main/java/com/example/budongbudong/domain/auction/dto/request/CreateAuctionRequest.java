package com.example.budongbudong.domain.auction.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CreateAuctionRequest {

    @NotBlank(message = "매물ID는 필수입니다.")
    private Long propertyId;

    @NotBlank(message = "시작가는 필수입니다.")
    @Positive(message = "시작가는 0보다 커야 합니다.")
    private Long startPrice;

    @NotBlank(message = "경매 시작 시간은 필수입니다.")
    private LocalDateTime startedAt;

    @NotBlank(message = "경매 종료 시간은 필수입니다.")
    private LocalDateTime endedAt;

}