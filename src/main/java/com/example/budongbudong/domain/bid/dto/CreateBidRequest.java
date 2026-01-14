package com.example.budongbudong.domain.bid.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class CreateBidRequest {

    @NotNull(message = "금액은 필수 입력값입니다.")
    @Positive(message = "입찰 금액은 0보다 커야 합니다.")
    private Long price;
}
