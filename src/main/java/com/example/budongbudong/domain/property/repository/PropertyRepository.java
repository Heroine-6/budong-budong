package com.example.budongbudong.domain.property.repository;

import com.example.budongbudong.domain.property.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property,Long> {
}
