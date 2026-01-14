package com.example.budongbudong.domain.property.controller;

import com.example.budongbudong.common.response.CustomPageResponse;
import com.example.budongbudong.domain.property.dto.PropertyResponse;
import com.example.budongbudong.domain.property.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/properties")
public class PropertyController {

    private final PropertyService propertyService;

    @GetMapping
    public ResponseEntity<CustomPageResponse<PropertyResponse>> getAllPropertyList(
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        CustomPageResponse<PropertyResponse> response = propertyService.getAllPropertyList(pageable);
        return ResponseEntity.ok(response);
    }
}
