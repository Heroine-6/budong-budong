package com.example.budongbudong.domain.propertyimage.dto;

import com.example.budongbudong.common.entity.PropertyImage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PropertyImageResponse {

    private final Long id;
    private final String imageUrl;

    public static PropertyImageResponse from(PropertyImage image) {
        return new PropertyImageResponse(
                image.getId(),
                image.getImageUrl()
        );
    }
}
