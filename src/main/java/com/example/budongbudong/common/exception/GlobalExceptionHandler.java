package com.example.budongbudong.common.exception;

import com.example.budongbudong.common.dto.ValidationErrorResponse;
import com.example.budongbudong.common.response.GlobalResponse;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<GlobalResponse<Void>> handleException(CustomException e) {
        log.error("예외 발생. ", e);
        GlobalResponse<Void> response = GlobalResponse.exception( e.getErrorCode(), null);
        return ResponseEntity.status(e.getErrorCode().getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponse<List<ValidationErrorResponse>>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {

        List<ValidationErrorResponse> errors =
                        e.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(fieldError -> new ValidationErrorResponse(
                                        fieldError.getField(),
                                        fieldError.getDefaultMessage()
                        ))
                        .toList();
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        GlobalResponse<List<ValidationErrorResponse>> response =
                GlobalResponse.exception(errorCode, errors);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ExceptionHandler({LockTimeoutException.class, PessimisticLockException.class, CannotAcquireLockException.class})
    public ResponseEntity<GlobalResponse<Void>> handleLockExceptions(Exception e) {
        log.error("락 관련 예외 발생. ", e);
        ErrorCode errorCode = ErrorCode.BID_LOCK_TIMEOUT;
        GlobalResponse<Void> response = GlobalResponse.exception(errorCode, null);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }
}
