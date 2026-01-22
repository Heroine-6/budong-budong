package com.example.budongbudong.domain.propertyimage.service;

import com.example.budongbudong.common.storage.StorageRetryKeys;
import com.example.budongbudong.common.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
/**
 * - Redis 큐에서 하나씩 꺼내서 삭제 재시도
 * - 실패하면 횟수 감소 후 다시 큐에 넣음
 * - 남은 횟수가 0이면 큐에서 제거하고 에러 로그만 남김
 * - 기본 주기: 1시간, 배치 크기: 50건
 */
public class ImageDeleteRetryScheduler {

    private static final int DEFAULT_BATCH_SIZE = 50;

    private final StorageService storageService;
    private final RedisTemplate<String, String> redisTemplate;

    // Redis 큐에 쌓인 삭제 실패 URL을 일정 주기로 재시도한다.
    @Scheduled(fixedDelayString = "${storage.delete-retry-interval-ms:3600000}")
    public void retryDeletes() {
        for (int i = 0; i < DEFAULT_BATCH_SIZE; i++) {
            String payload = redisTemplate.opsForList().leftPop(StorageRetryKeys.S3_DELETE_RETRY);
            if (payload == null) {
                return;
            }

            // payload: "남은횟수|URL"
            ParsedRetry parsed = ParsedRetry.from(payload);
            if (parsed == null) {
                log.warn("[IMAGE] 삭제 재시도 payload 파싱 실패 - payload={}", payload);
                continue;
            }

            try {
                storageService.delete(parsed.url());
                log.info("[IMAGE] 삭제 재시도 성공 - url={}", parsed.url());
            } catch (Exception e) {
                int remaining = parsed.remaining() - 1;
                if (remaining <= 0) {
                    log.error("[IMAGE] 삭제 재시도 횟수 초과 - url={}", parsed.url(), e);
                    return;
                }

                // 실패하면 횟수를 줄여 다시 큐에 넣는다.
                String nextPayload = remaining + "|" + parsed.url();
                log.warn("[IMAGE] 삭제 재시도 실패 - url={}, remaining={}", parsed.url(), remaining, e);
                redisTemplate.opsForList().rightPush(StorageRetryKeys.S3_DELETE_RETRY, nextPayload);
                return;
            }
        }
    }

    private record ParsedRetry(int remaining, String url) {
        static ParsedRetry from(String payload) {
            int sep = payload.indexOf('|');
            if (sep <= 0 || sep == payload.length() - 1) {
                return null;
            }

            try {
                int remaining = Integer.parseInt(payload.substring(0, sep));
                String url = payload.substring(sep + 1);
                return new ParsedRetry(remaining, url);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
}
