package com.example.budongbudong.domain.propertyimage.dto;

import com.example.budongbudong.common.entity.PropertyImage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PropertyImageResponse {

    private final String imageUrl;

    public static PropertyImageResponse from(PropertyImage image) {
        return new PropertyImageResponse(
                image.getImageUrl()
        );
    }

    /**
     * QueryDsl용 (대표 이미지)
     */
    public static PropertyImageResponse from(String imageUrl) {
        return new PropertyImageResponse(
                imageUrl
        );
    }
}
