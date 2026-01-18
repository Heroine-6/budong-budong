package com.example.budongbudong.domain.property.repository;

import com.example.budongbudong.domain.property.dto.response.ReadAllPropertyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QPropertyRepository {
    Page<ReadAllPropertyResponse> findAllProperties(Pageable pageable);
}
