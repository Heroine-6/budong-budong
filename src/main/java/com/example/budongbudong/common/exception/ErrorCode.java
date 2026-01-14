package com.example.budongbudong.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    //------409-----------------------
    USER_ALREADY_EXISTS(409, "이미 존재하는 사용자 이메일입니다."),
    BID_PRICE_TOO_LOW(409, "입찰가는 현재 최고가보다 높아야 합니다."),

    //------404-----------------------
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    AUCTION_NOT_FOUND(404, "존재하지 않는 경매입니다."),

    //------403-----------------------
    USER_NOT_MATCH(403, "접근 권한이 없습니다"),
    PASSWORD_NOT_MATCH(403, "비밀번호가 일치하지 않습니다."),
    FORBIDDEN(403, "접근 권한이 없습니다"),
    AUCTION_NOT_OPEN(403, "현재 입찰이 불가능한 경매입니다."),

    //------401-----------------------
    LOGIN_REQUIRED(401, "로그인한 유저만 사용할 수 있는 기능입니다"),
    LOGIN_UNAUTHORIZED(401, "아이디 또는 비밀번호가 올바르지 않습니다."),

    //------400-----------------------
    INVALID_EMAIL_FORMAT(400, "이메일 형식이 올바르지 않습니다."),
    INVALID_PASSWORD_FORMAT(400, "비밀번호 형식이 올바르지 않습니다."),
    INVALID_PASSWORD(400, "비밀번호가 유효하지 않습니다."),
    VALIDATION_ERROR(400, "입력값이 유효하지 않습니다."),
    INVALID_BID_PRICE(400, "입찰 금액이 올바르지 않습니다."),

    ;
    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
