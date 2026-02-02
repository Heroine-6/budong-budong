package com.example.budongbudong.domain.payment.toss.exception;

/**
 * 토스 승인 요청이 명확히 거절된 경우
 * - 승인 실패 확정 가능
 */
public class TossClientException extends RuntimeException{
    public TossClientException() {
        super();
    }

    public TossClientException(String message) {
        super(message);
    }
}
