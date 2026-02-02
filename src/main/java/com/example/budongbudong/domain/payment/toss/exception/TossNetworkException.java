package com.example.budongbudong.domain.payment.toss.exception;

/**
 * 토스 승인 결과를 확정할 수 없는 경우
 * - 타임아웃, 연결 실패, 서버 오류 등
 * - 재확인 필요
 */
public class TossNetworkException extends  RuntimeException{

    public TossNetworkException() {
        super();
    }

    public TossNetworkException(String message) {
        super(message);
    }

    public TossNetworkException(Throwable cause) {
        super(cause);
    }
}
