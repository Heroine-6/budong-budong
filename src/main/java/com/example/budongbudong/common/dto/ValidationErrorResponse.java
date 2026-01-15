package com.example.budongbudong.common.dto;

public record ValidationErrorResponse(
        String field,
        String message
) {}
