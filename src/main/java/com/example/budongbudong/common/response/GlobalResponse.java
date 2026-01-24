package com.example.budongbudong.common.response;

import com.example.budongbudong.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
    public static <T> GlobalResponse<T> success(T data) {
        return new GlobalResponse<>(true, null, data); //204 는 data null로 넣어주세요.
    }

    //예외처리시
    public static <T> GlobalResponse<T> exception(ErrorCode errorCode, T data) {
        return new GlobalResponse<>(false, errorCode.getMessage(), data);
    }

    // 로직 실패 시 (200 응답용)
    public static <T> GlobalResponse<T> successButRejected(String message) {
        return new GlobalResponse<>(false, message, null);
    }

    public static <T> ResponseEntity<GlobalResponse<T>> ok(T data) {
        return ResponseEntity.ok(success(data));
    }

    public static <T> ResponseEntity<GlobalResponse<T>> okButRejected(String message) {
        return ResponseEntity.ok(successButRejected(message));
    }

    public static <T> ResponseEntity<GlobalResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(success(data));
    }

    public static <T> ResponseEntity<GlobalResponse<T>> noContent() {
        return ResponseEntity.noContent().build();
    }
}
