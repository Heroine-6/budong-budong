package com.example.budongbudong.domain.property.repository;

import com.example.budongbudong.domain.property.entity.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property,Long> {

    @Query("""
    select p from Property p
    left join fetch p.propertyImageList pi
    where p.id = :propertyId
    """)
    Optional<Property> findByIdWithImages(@Param("propertyId") Long propertyId);

    Page<Property> findAllByUserId(Long userId, Pageable pageable);

    Optional<Property> findByIdAndIsDeletedFalse(Long propertyId);
}
