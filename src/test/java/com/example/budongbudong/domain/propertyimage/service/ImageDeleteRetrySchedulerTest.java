package com.example.budongbudong.domain.propertyimage.service;

import com.example.budongbudong.common.storage.StorageRetryKeys;
import com.example.budongbudong.common.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageDeleteRetrySchedulerTest {

    @Mock
    private StorageService storageService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ListOperations<String, String> listOperations;

    @InjectMocks
    private ImageDeleteRetryScheduler scheduler;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    @Test
    @DisplayName("삭제 실패 시 횟수를 감소시켜 큐에 재삽입한다")
    void retryDeletes_requeueWhenDeleteFails() {
        // given
        String url = "https://s3.amazonaws.com/bucket/c.jpg";
        when(listOperations.leftPop(StorageRetryKeys.S3_DELETE_RETRY))
                .thenReturn("3|" + url)
                .thenReturn(null);
        doThrow(new RuntimeException("delete fail")).when(storageService).delete(url);

        // when
        scheduler.retryDeletes();

        // then
        verify(listOperations).rightPush(StorageRetryKeys.S3_DELETE_RETRY, "2|" + url);
    }

    @Test
    @DisplayName("재시도 횟수 초과 항목은 건너뛰고 다음 항목을 처리한다")
    void retryDeletes_skipWhenRetryExhausted() {
        // given
        String exhaustedUrl = "https://s3.amazonaws.com/bucket/exhausted.jpg";
        String okUrl = "https://s3.amazonaws.com/bucket/ok.jpg";
        when(listOperations.leftPop(StorageRetryKeys.S3_DELETE_RETRY))
                .thenReturn("1|" + exhaustedUrl)
                .thenReturn("3|" + okUrl)
                .thenReturn(null);
        doThrow(new RuntimeException("delete fail")).when(storageService).delete(exhaustedUrl);

        // when
        scheduler.retryDeletes();

        // then
        verify(storageService).delete(okUrl);
    }

    @Test
    @DisplayName("삭제 실패 후 재삽입하더라도 다음 항목을 계속 처리한다")
    void retryDeletes_continueAfterRequeue() {
        // given
        String failedUrl = "https://s3.amazonaws.com/bucket/failed.jpg";
        String okUrl = "https://s3.amazonaws.com/bucket/ok-next.jpg";
        when(listOperations.leftPop(StorageRetryKeys.S3_DELETE_RETRY))
                .thenReturn("3|" + failedUrl)
                .thenReturn("3|" + okUrl)
                .thenReturn(null);
        doThrow(new RuntimeException("delete fail")).when(storageService).delete(failedUrl);

        // when
        scheduler.retryDeletes();

        // then
        verify(storageService).delete(okUrl);
    }

    @Test
    @DisplayName("삭제 성공 시 재삽입하지 않는다")
    void retryDeletes_noRequeueWhenDeleteSucceeds() {
        // given
        String url = "https://s3.amazonaws.com/bucket/d.jpg";
        when(listOperations.leftPop(StorageRetryKeys.S3_DELETE_RETRY))
                .thenReturn("3|" + url)
                .thenReturn(null);

        // when
        scheduler.retryDeletes();

        // then
        verify(listOperations, never()).rightPush(StorageRetryKeys.S3_DELETE_RETRY, "2|" + url);
    }
}
