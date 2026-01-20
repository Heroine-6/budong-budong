package com.example.budongbudong.domain.auth.dto.request;

public record SmsVerifyRequest(String toNumber, String code) { }