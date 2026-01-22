package com.example.budongbudong.domain.propertyimage.service;

import com.example.budongbudong.common.entity.Property;
import com.example.budongbudong.common.entity.PropertyImage;
import com.example.budongbudong.common.storage.StorageService;
import com.example.budongbudong.common.storage.StorageRetryKeys;
import com.example.budongbudong.domain.propertyimage.repository.PropertyImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
/**
 * 이미지 실패 처리 흐름
 * 1. 이미지 업로드/URL 저장 시작 -> DB 저장 중 예외 발생
 * 2. 지금까지 업로드된 URL을 전부 순회하면서 삭제 시도
 * 3. 삭제 실패는 로그로 기록
 * 4. 원래 예외는 그대로 던짐 (트랜잭션 롤백)
 *
 * - 부분 실패 시 고아 파일 최소화
 * - 다만 삭제도 실패할 수 있으니 그때는 로그만 남고 넘어갑니다.
 *
 * 250122
 * s3찌꺼기들도 레디스로 삭제하는 것으로 구현
 * - 삭제 실패 시 URL을 Redis 큐에 저장
 * - 큐 저장 포맷: 3|https://... (재시도 횟수 + URL)
 */
public class PropertyImageService {

    private final PropertyImageRepository propertyImageRepository;
    private final StorageService storageService;
    private final RedisTemplate<String, String> redisTemplate;

    @Transactional
    public void saveImages(Property property, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return;
        }

        List<String> uploadedUrls = new java.util.ArrayList<>();

        try {
            for (MultipartFile image : images) {
                String imageUrl = storageService.upload(image, "properties");
                uploadedUrls.add(imageUrl);

                PropertyImage propertyImage = PropertyImage.builder()
                        .property(property)
                        .imageUrl(imageUrl)
                        .build();

                propertyImageRepository.save(propertyImage);

                // 매물 엔티티 쪽에도 이미지 추가 (양방향 연관관계 맞추기)
                property.addImage(propertyImage);
            }
        } catch (Exception e) {
            for (String url : uploadedUrls) {
                try {
                    storageService.delete(url);
                } catch (Exception deleteError) {
                    log.error("[IMAGE] 업로드 보상 삭제 실패 - url={}", url, deleteError);
                    enqueueDeleteRetry(url);
                }
            }
            throw e;
        }
    }

    @Transactional
    public void saveImageUrls(Property property, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        List<String> uploadedUrls = new java.util.ArrayList<>(imageUrls);

        try {
            for (String imageUrl : imageUrls) {
                PropertyImage propertyImage = PropertyImage.builder()
                        .property(property)
                        .imageUrl(imageUrl)
                        .build();

                propertyImageRepository.save(propertyImage);
                property.addImage(propertyImage);
            }
        } catch (Exception e) {
            for (String url : uploadedUrls) {
                try {
                    storageService.delete(url);
                } catch (Exception deleteError) {
                    log.error("[IMAGE] 업로드 보상 삭제 실패 - url={}", url, deleteError);
                    enqueueDeleteRetry(url);
                }
            }
            throw e;
        }
    }

    private void enqueueDeleteRetry(String url) {
        try {
            // "재시도 횟수|URL" 형태로 큐에 저장
            String payload = StorageRetryKeys.DEFAULT_RETRY_COUNT + "|" + url;
            redisTemplate.opsForList().rightPush(StorageRetryKeys.S3_DELETE_RETRY, payload);
        } catch (Exception e) {
            log.error("[IMAGE] 삭제 재시도 큐 저장 실패 - url={}", url, e);
        }
    }
}
