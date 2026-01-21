package com.example.budongbudong.domain.property.repository;

import com.example.budongbudong.domain.property.dto.response.ReadAllPropertyResponse;
import org.springframework.data.domain.*;

public interface QPropertyRepository {

    Page<ReadAllPropertyResponse> findAllMyProperties(Long userId, Pageable pageable);

    Slice<ReadAllPropertyResponse> findPropertyList(Pageable pageable);
}
