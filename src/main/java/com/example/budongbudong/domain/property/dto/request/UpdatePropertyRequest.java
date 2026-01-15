package com.example.budongbudong.domain.property.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public class UpdatePropertyRequest {

    @NotNull(message = "경매 시작가는 필수 입력값입니다.")
    @Positive(message = "경매 시작가는 0보다 커야 합니다.")
    private Long price;

    @NotNull(message = "입주 가능 날짜는 필수 입력값입니다.")
    @Future(message = "입주 가능 날짜는 미래 날짜여야 합니다.")
    private LocalDate migrateDate;

    private String description;

    // TODO: 매물 등록 구현 후 맞춰서 이미지 수정 기능 추가
}
