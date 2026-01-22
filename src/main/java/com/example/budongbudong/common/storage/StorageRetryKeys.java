package com.example.budongbudong.common.storage;

/**
 * Redis 큐 키 상수와 기본 재시도 횟수를 관리
 */
public final class StorageRetryKeys {

    private StorageRetryKeys() {
    }

    // S3 삭제 재시도 큐 키
    public static final String S3_DELETE_RETRY = "S3:DELETE:RETRY";
    // 기본 재시도 횟수
    public static final int DEFAULT_RETRY_COUNT = 3;
}
