package com.example.budongbudong.domain.property.repository;

import com.example.budongbudong.domain.property.entity.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property,Long>, QPropertyRepository {

    @Query("""
    select distinct p from Property p
    left join fetch p.propertyImageList pi
    where p.id = :propertyId
    and p.isDeleted = false
    """)
    Optional<Property> findByIdWithImagesAndNotDeleted(@Param("propertyId") Long propertyId);

    Page<Property> findAllByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    Optional<Property> findByIdAndIsDeletedFalse(Long propertyId);
}
