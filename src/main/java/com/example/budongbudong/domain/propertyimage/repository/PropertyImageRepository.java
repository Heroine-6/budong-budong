package com.example.budongbudong.domain.propertyimage.repository;

import com.example.budongbudong.common.entity.PropertyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PropertyImageRepository extends JpaRepository<PropertyImage, Long> {

    @Query("""
            select pi
            from PropertyImage pi
            where pi.property.id in :propertyIds
              and pi.isDeleted = false
            order by pi.createdAt asc
        """)
    List<PropertyImage> findThumbnailImagesByPropertyIds(@Param("propertyIds") List<Long> propertyIds);

    Optional<PropertyImage> findFirstByPropertyIdOrderByCreatedAtAsc(Long propertyId);

    default String findThumbnailImageUrl(Long propertyId) {
        return findFirstByPropertyIdOrderByCreatedAtAsc(propertyId)
                .map(PropertyImage::getImageUrl)
                .orElse(null);
    }
}
