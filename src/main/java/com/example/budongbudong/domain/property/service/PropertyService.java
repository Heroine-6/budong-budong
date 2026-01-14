package com.example.budongbudong.domain.property.service;

import com.example.budongbudong.common.response.CustomPageResponse;
import com.example.budongbudong.domain.property.dto.PropertyResponse;
import com.example.budongbudong.domain.property.entity.Property;
import com.example.budongbudong.domain.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PropertyService {
    private final PropertyRepository propertyRepository;

    @Transactional(readOnly = true)
    public CustomPageResponse<PropertyResponse> getAllPropertyList(Pageable pageable) {

        Page<Property> propertyPage = propertyRepository.findAll(pageable);
        Page<PropertyResponse> response = propertyPage.map(PropertyResponse::from);
        return CustomPageResponse.from(response);
    }
}
