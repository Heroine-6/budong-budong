package com.example.budongbudong.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    //------409-----------------------
    USER_ALREADY_EXISTS(409, "이미 존재하는 사용자 이메일입니다."),
    AUCTION_ALREADY_EXISTS(409, "이미 경매가 진행중인 매물입니다."),
    BID_PRICE_TOO_LOW(409, "입찰가는 현재 최고가보다 높아야 합니다."),
    AUCTION_INVALID_STATUS_FOR_CANCEL(409, "경매 시작 전 상태에서만 경매를 취소할 수 있습니다."),
    BID_LOCK_TIMEOUT(409, "입찰 요청이 몰려 잠시 후 다시 시도해주세요."),

    //------404-----------------------
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    AUCTION_NOT_FOUND(404, "존재하지 않는 경매입니다."),
    PROPERTY_NOT_FOUND(404, "존재하지 않는 매물입니다."),
    EXTERNAL_API_FAILED(404, "해당 정보가 없습니다."),

    //------403-----------------------
    USER_NOT_MATCH(403, "소유자만 접근 가능합니다."),
    PASSWORD_NOT_MATCH(403, "비밀번호가 일치하지 않습니다."),
    FORBIDDEN(403, "접근 권한이 없습니다"),
    AUCTION_NOT_OPEN(403, "현재 입찰이 불가능한 경매입니다."),

    //------401-----------------------
    LOGIN_REQUIRED(401, "로그인한 유저만 사용할 수 있는 기능입니다"),
    LOGIN_UNAUTHORIZED(401, "아이디 또는 비밀번호가 올바르지 않습니다."),
    SMS_VERIFICATION_REQUIRED(401, "휴대전화 인증을 먼저 완료해주세요."),
    TOKEN_EXPIRED(401, "Token이 만료되었습니다."),
    TOKEN_INVALID(401, "Token이 유효하지 않습니다."),

    //------400-----------------------
    INVALID_ADDRESS(400, "주소에서 지역코드를 찾을 수 없습니다. 시/군/구를 포함한 전체 주소를 입력해주세요."),
    API_PROPERTY_NOT_MATCHED(400, "입력한 주소와 층수에 해당하는 실거래 정보를 찾을 수 없습니다."),
    INVALID_EMAIL_FORMAT(400, "이메일 형식이 올바르지 않습니다."),
    INVALID_PASSWORD_FORMAT(400, "비밀번호 형식이 올바르지 않습니다."),
    INVALID_PASSWORD(400, "비밀번호가 유효하지 않습니다."),
    VALIDATION_ERROR(400, "입력값이 유효하지 않습니다."),
    INVALID_BID_PRICE(400, "입찰 금액이 올바르지 않습니다."),
    INVALID_REQUEST(400, "입력값 형식 오류"),
    INVALID_AUCTION_PERIOD(400, "경매 기간이 유효하지 않습니다."),
    PROPERTY_CANNOT_DELETE(400, "경매 시작 전 상태에서만 매물을 삭제할 수 있습니다."),
    INVALID_PRICE_RANGE(400, "최소 가격은 최대 가격보다 클 수 없습니다"),
    SMS_CODE_EXPIRED(400, "인증번호가 만료되었습니다."),
    SMS_CODE_MISMATCH(400, "인증번호가 올바르지 않습니다."),
    S3_NOT_CONFIGURED(400, "S3 설정이 필요합니다."),


    //------500-----------------------
    FILE_UPLOAD_FAILED(500, "파일 업로드에 실패했습니다."),
    FILE_DELETE_FAILED(500, "파일 삭제에 실패했습니다."),
    SMS_SEND_FAILED(500, "인증번호 전송에 실패했습니다."),

    ;
    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
