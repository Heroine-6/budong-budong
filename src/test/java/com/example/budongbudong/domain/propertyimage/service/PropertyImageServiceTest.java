package com.example.budongbudong.domain.propertyimage.service;

import com.example.budongbudong.common.entity.Property;
import com.example.budongbudong.common.storage.StorageRetryKeys;
import com.example.budongbudong.common.storage.StorageService;
import com.example.budongbudong.domain.propertyimage.repository.PropertyImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PropertyImageServiceTest {

    @Mock
    private PropertyImageRepository propertyImageRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ListOperations<String, String> listOperations;

    @InjectMocks
    private PropertyImageService propertyImageService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    @Test
    @DisplayName("이미지 업로드 후 DB 저장 실패 시 삭제 재시도 큐에 적재한다")
    void saveImages_enqueueRetryWhenDeleteFails() {
        // given
        MultipartFile file = new MockMultipartFile(
                "images",
                "a.jpg",
                "image/jpeg",
                "data".getBytes()
        );
        String url = "https://s3.amazonaws.com/bucket/a.jpg";

        when(storageService.upload(any(MultipartFile.class), eq("properties"))).thenReturn(url);
        when(propertyImageRepository.save(any())).thenThrow(new RuntimeException("db fail"));
        doThrow(new RuntimeException("delete fail")).when(storageService).delete(url);

        Property property = mock(Property.class);

        // when & then
        assertThatThrownBy(() -> propertyImageService.saveImages(property, List.of(file)))
                .isInstanceOf(RuntimeException.class);

        verify(listOperations).rightPush(
                StorageRetryKeys.S3_DELETE_RETRY,
                StorageRetryKeys.DEFAULT_RETRY_COUNT + "|" + url
        );
    }

    @Test
    @DisplayName("이미지 URL 저장 실패 시 삭제 재시도 큐에 적재한다")
    void saveImageUrls_enqueueRetryWhenDeleteFails() {
        // given
        String url = "https://s3.amazonaws.com/bucket/b.jpg";

        when(propertyImageRepository.save(any())).thenThrow(new RuntimeException("db fail"));
        doThrow(new RuntimeException("delete fail")).when(storageService).delete(url);

        Property property = mock(Property.class);

        // when & then
        assertThatThrownBy(() -> propertyImageService.saveImageUrls(property, List.of(url)))
                .isInstanceOf(RuntimeException.class);

        verify(listOperations).rightPush(
                StorageRetryKeys.S3_DELETE_RETRY,
                StorageRetryKeys.DEFAULT_RETRY_COUNT + "|" + url
        );
    }
}
