package com.example.budongbudong.domain.property.repository;

import com.example.budongbudong.domain.property.entity.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property,Long> {
    Page<Property> findAllByUserId(Long userId, Pageable pageable);
}
