package com.example.budongbudong.domain.property.service;

import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.common.response.CustomPageResponse;
import com.example.budongbudong.domain.property.dto.response.ReadAllPropertyResponse;
import com.example.budongbudong.domain.property.dto.response.ReadPropertyResponse;
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
    public CustomPageResponse<ReadAllPropertyResponse> getAllPropertyList(Pageable pageable) {

        Page<Property> propertyPage = propertyRepository.findAll(pageable);
        Page<ReadAllPropertyResponse> response = propertyPage.map(ReadAllPropertyResponse::from);
        return CustomPageResponse.from(response);
    }

    @Transactional(readOnly = true)
    public CustomPageResponse<ReadAllPropertyResponse> getMyPropertyList(Long userId, Pageable pageable) {

        Page<Property> propertyPage = propertyRepository.findAllByUserId(userId, pageable);
        Page<ReadAllPropertyResponse> response = propertyPage.map(ReadAllPropertyResponse::from);

        return CustomPageResponse.from(response);
    }

    @Transactional(readOnly = true)
    public ReadPropertyResponse getProperty(Long propertyId) {

        Property property = propertyRepository.findByIdWithImages(propertyId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROPERTY_NOT_FOUND));

        return ReadPropertyResponse.from(property);
    }
}
