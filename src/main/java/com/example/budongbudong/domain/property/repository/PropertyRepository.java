package com.example.budongbudong.domain.property.repository;

import com.example.budongbudong.common.entity.Property;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property, Long>, QPropertyRepository {

    @Query("""
            select distinct p from Property p
            left join fetch p.propertyImageList pi
            where p.id = :propertyId
            and p.isDeleted = false
            """)
    Optional<Property> findByIdWithImagesAndNotDeleted(@Param("propertyId") Long propertyId);

    Optional<Property> findByIdAndIsDeletedFalse(Long propertyId);

    default Property getByIdWithImagesAndNotDeletedOrThrow(Long propertyId) {
        return findByIdWithImagesAndNotDeleted(propertyId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROPERTY_NOT_FOUND));
    }

    default Property getByIdAndNotDeletedOrThrow(Long propertyId) {
        return findByIdAndIsDeletedFalse(propertyId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROPERTY_NOT_FOUND));
    }
}
