package com.example.budongbudong.domain.property.controller;

import com.example.budongbudong.common.dto.AuthUser;
import com.example.budongbudong.common.response.CustomPageResponse;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.property.dto.request.CreatePropertyRequest;
import com.example.budongbudong.domain.property.dto.request.UpdatePropertyRequest;
import com.example.budongbudong.domain.property.dto.response.ReadAllPropertyResponse;
import com.example.budongbudong.domain.property.dto.response.ReadPropertyResponse;
import com.example.budongbudong.domain.property.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse<Void>> createProperty(
            @Valid @ModelAttribute CreatePropertyRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        propertyService.createProperty(request, images, authUser.getUserId());

        return GlobalResponse.created(null);
    }

    @GetMapping
    public ResponseEntity<GlobalResponse<CustomPageResponse<ReadAllPropertyResponse>>> getAllPropertyList(
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        CustomPageResponse<ReadAllPropertyResponse> response = propertyService.getAllPropertyList(pageable);
        return GlobalResponse.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<GlobalResponse<CustomPageResponse<ReadAllPropertyResponse>>> getMyPropertyList(
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        CustomPageResponse<ReadAllPropertyResponse> response = propertyService.getMyPropertyList(authUser.getUserId(), pageable);
        return GlobalResponse.ok(response);
    }

    @GetMapping("/{propertyId}")
    public ResponseEntity<GlobalResponse<ReadPropertyResponse>> getProperty(@PathVariable Long propertyId) {

        ReadPropertyResponse response = propertyService.getProperty(propertyId);

        return GlobalResponse.ok(response);
    }

    @PatchMapping("/{propertyId}")
    public ResponseEntity<GlobalResponse<Void>> updateProperty(
            @Valid @RequestBody UpdatePropertyRequest request,
            @PathVariable Long propertyId,
            @AuthenticationPrincipal AuthUser authUser
    ) {

        propertyService.updateProperty(propertyId, request, authUser.getUserId());

        return GlobalResponse.noContent();
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<GlobalResponse<Void>> deleteProperty(@PathVariable Long propertyId, @AuthenticationPrincipal AuthUser authUser) {

        propertyService.deleteProperty(propertyId, authUser.getUserId());

        return GlobalResponse.noContent();
    }
}
