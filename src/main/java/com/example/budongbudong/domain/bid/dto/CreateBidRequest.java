package com.example.budongbudong.domain.bid.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateBidRequest {

    @NotNull(message = "금액은 필수 입력값입니다.")
    private Long price;
}
