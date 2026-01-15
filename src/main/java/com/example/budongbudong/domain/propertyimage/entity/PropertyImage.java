package com.example.budongbudong.domain.propertyimage.entity;

import com.example.budongbudong.common.entity.BaseEntity;
import com.example.budongbudong.domain.property.entity.Property;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="property_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PropertyImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="property_id",nullable = false)
    private Property property;

    @Column(name = "image_url",nullable = false,length = 255)
    private String imageUrl;

    @Builder
    public PropertyImage(Property property, String imageUrl) {
        this.property = property;
        this.imageUrl = imageUrl;
    }
}
