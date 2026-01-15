package com.example.budongbudong.domain.property.controller;

import com.example.budongbudong.common.response.CustomPageResponse;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.property.dto.request.UpdatePropertyRequest;
import com.example.budongbudong.domain.property.dto.response.ReadAllPropertyResponse;
import com.example.budongbudong.domain.property.dto.response.ReadPropertyResponse;
import com.example.budongbudong.domain.property.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/properties")
public class PropertyController {

    private final PropertyService propertyService;

    @GetMapping
    public ResponseEntity<CustomPageResponse<ReadAllPropertyResponse>> getAllPropertyList(
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        CustomPageResponse<ReadAllPropertyResponse> response = propertyService.getAllPropertyList(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<CustomPageResponse<ReadAllPropertyResponse>> getMyPropertyList(
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        //TODO 시큐리티 구현 후 AuthUser의 id로 수정 예정
        CustomPageResponse<ReadAllPropertyResponse> response = propertyService.getMyPropertyList(7L,pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{propertyId}")
    public ResponseEntity<GlobalResponse<ReadPropertyResponse>> getProperty(@PathVariable Long propertyId) {

        ReadPropertyResponse response = propertyService.getProperty(propertyId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(GlobalResponse.success(
                        true,
                        "매물 정보가 성공적으로 조회되었습니다.",
                        response
                ));
    }

    @PatchMapping("/{propertyId}")
    public ResponseEntity<GlobalResponse<Void>> updateProperty(@Valid @RequestBody UpdatePropertyRequest request, @PathVariable Long propertyId) {

        propertyService.updateProperty(propertyId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(GlobalResponse.success(
                        true,
                        "매물 정보가 성공적으로 수정되었습니다.",
                        null
                ));
    }
}
