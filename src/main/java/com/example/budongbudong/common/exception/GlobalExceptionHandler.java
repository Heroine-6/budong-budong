package com.example.budongbudong.common.exception;

import com.example.budongbudong.common.response.GlobalResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<GlobalResponse<Void>> handleException(CustomException e) {
        log.error("예외 발생. ", e);
        GlobalResponse<Void> response = GlobalResponse.exception(false, e.getErrorCode());
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("예외 발생. ", e);

        String detailMessage = e.getFieldError() != null
                ? e.getFieldError().getDefaultMessage()
                : "";

        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        String finalMessage = errorCode.getMessage() + detailMessage;

        GlobalResponse<Void> response = GlobalResponse.exception(false, errorCode);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }
}
