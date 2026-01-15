package com.example.budongbudong.common.response;

import com.example.budongbudong.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GlobalResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public GlobalResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    //성공시
    public static <T> GlobalResponse<T> success(boolean success, String message, T data) {
        return new GlobalResponse<>(success, message, data); //204 는 data null로 넣어주세요.
    }

    //예외처리시
    public static <T> GlobalResponse<T> exception(boolean success, ErrorCode errorCode, T data) {
        return new GlobalResponse<>(success, errorCode.getMessage(), data);
    }
}
