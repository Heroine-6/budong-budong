package com.example.budongbudong.domain.propertyimage.service;

import com.example.budongbudong.common.storage.StorageService;
import com.example.budongbudong.domain.property.entity.Property;
import com.example.budongbudong.domain.propertyimage.entity.PropertyImage;
import com.example.budongbudong.domain.propertyimage.repository.PropertyImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyImageService {

    private final PropertyImageRepository propertyImageRepository;
    private final StorageService storageService;

    @Transactional
    public void saveImages(Property property, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return;
        }

        for (MultipartFile image : images) {
            String imageUrl = storageService.upload(image, "properties");

            PropertyImage propertyImage = PropertyImage.builder()
                    .property(property)
                    .imageUrl(imageUrl)
                    .build();

            propertyImageRepository.save(propertyImage);

            // 매물 엔티티 쪽에도 이미지 추가 (양방향 연관관계 맞추기)
            property.addImage(propertyImage);
        }
    }
}
