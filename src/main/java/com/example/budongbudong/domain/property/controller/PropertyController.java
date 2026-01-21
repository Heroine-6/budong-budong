package com.example.budongbudong.domain.property.controller;

import com.example.budongbudong.common.dto.AuthUser;
import com.example.budongbudong.common.response.*;
import com.example.budongbudong.common.storage.PresignedUrlInfo;
import com.example.budongbudong.domain.property.dto.condition.SearchPropertyCond;
import com.example.budongbudong.domain.property.dto.request.CreatePropertyRequest;
import com.example.budongbudong.domain.property.dto.request.PresignedUrlRequest;
import com.example.budongbudong.domain.property.dto.request.UpdatePropertyRequest;
import com.example.budongbudong.domain.property.dto.response.*;
import com.example.budongbudong.domain.property.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final PropertyImagePresignService propertyImagePresignService;
    private final PropertySyncService propertySyncService;
    private final PropertySearchService propertySearchService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GlobalResponse<Void>> createProperty(
            @Valid @ModelAttribute CreatePropertyRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "imageUrls", required = false) List<String> imageUrls,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        propertyService.createProperty(request, images, imageUrls, authUser.getUserId());

        return GlobalResponse.created(null);
    }

    @PostMapping("/images/presign")
    public ResponseEntity<GlobalResponse<List<PresignedUrlInfo>>> issuePresignedUrls(
            @Valid @RequestBody PresignedUrlRequest request
    ) {
        List<PresignedUrlInfo> response = propertyImagePresignService.issuePresignedUrls(request);
        return GlobalResponse.ok(response);
    }

    @GetMapping
    public ResponseEntity<GlobalResponse<CustomSliceResponse<ReadAllPropertyResponse>>> getAllPropertyList(
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        CustomSliceResponse<ReadAllPropertyResponse> response = propertyService.getAllPropertyList(pageable);
        return GlobalResponse.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<GlobalResponse<CustomSliceResponse<SearchPropertyResponse>>> search(
            @ModelAttribute SearchPropertyCond cond,
            @PageableDefault(
                    page = 0,
                    size = 10
            )
            Pageable pageable
    ) {
        CustomSliceResponse<SearchPropertyResponse> response = propertySearchService.search(cond,pageable);
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

    @PostMapping("/sync")
    public ResponseEntity<GlobalResponse<Void>> syncAllProperties() {

        propertySyncService.syncAllProperties();
        return GlobalResponse.noContent();
    }
}
