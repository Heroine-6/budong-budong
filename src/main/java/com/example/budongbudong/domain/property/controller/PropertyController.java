package com.example.budongbudong.domain.property.controller;

import com.example.budongbudong.common.dto.AuthUser;
import com.example.budongbudong.common.response.CustomPageResponse;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.property.dto.request.CreatePropertyRequestDTO;
import com.example.budongbudong.domain.property.dto.request.UpdatePropertyRequest;
import com.example.budongbudong.domain.property.dto.response.ReadAllPropertyResponse;
import com.example.budongbudong.domain.property.dto.response.ReadPropertyResponse;
import com.example.budongbudong.domain.property.enums.PropertyType;
import com.example.budongbudong.domain.property.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse<Void>> createProperty(
            @ModelAttribute CreatePropertyRequestDTO request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        propertyService.createProperty(request, images, authUser.getUserId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(GlobalResponse.success(true, "매물 등록 성공", null));
    }

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

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<GlobalResponse<Void>> deleteProperty(@PathVariable Long propertyId) {

        propertyService.deleteProperty(propertyId);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(GlobalResponse.success(
                        true,
                        "매물이 성공적으로 삭제되었습니다.",
                        null
                ));
    }
}
