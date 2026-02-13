package com.example.budongbudong.domain.property.controller;

import com.example.budongbudong.common.dto.AuthUser;
import com.example.budongbudong.common.response.CustomPageResponse;
import com.example.budongbudong.common.response.CustomSliceResponse;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.common.storage.PresignedUrlInfo;
import com.example.budongbudong.common.utils.annotation.SecurityNotRequired;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.property.dto.condition.SearchPropertyCond;
import com.example.budongbudong.domain.property.dto.request.CreatePropertyRequest;
import com.example.budongbudong.domain.property.dto.request.PresignedUrlRequest;
import com.example.budongbudong.domain.property.dto.request.PropertyLookupRequest;
import com.example.budongbudong.domain.property.dto.request.UpdatePropertyRequest;
import com.example.budongbudong.domain.property.dto.response.PropertyLookupResponse;
import com.example.budongbudong.domain.property.dto.response.ReadAllPropertyResponse;
import com.example.budongbudong.domain.property.dto.response.ReadPropertyResponse;
import com.example.budongbudong.domain.property.dto.response.SearchPropertyResponse;
import com.example.budongbudong.domain.property.enums.PropertyType;
import com.example.budongbudong.domain.property.service.PropertyImagePresignService;
import com.example.budongbudong.domain.property.service.PropertySearchService;
import com.example.budongbudong.domain.property.service.PropertyService;
import com.example.budongbudong.domain.property.service.PropertySyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "매물")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final PropertyImagePresignService propertyImagePresignService;
    private final PropertySyncService propertySyncService;
    private final PropertySearchService propertySearchService;

    @Operation(summary = "매물 등록", description = "매물 정보와 이미지를 함께 등록합니다. (SELLER 권한 필요)")
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

    @SecurityNotRequired
    @PostMapping("/v1/lookup")
    public ResponseEntity<GlobalResponse<PropertyLookupResponse>> lookupProperty(
            @Valid @RequestBody PropertyLookupRequest request
    ) {
        PropertyLookupResponse response = propertyService.lookupProperty(request);
        return GlobalResponse.ok(response);
    }

    @Operation(summary = "이미지 업로드 Presigned URL 발급", description = "S3 직접 업로드를 위한 Presigned URL을 발급합니다.")
    @PostMapping("/v1/images/presign")
    public ResponseEntity<GlobalResponse<List<PresignedUrlInfo>>> issuePresignedUrls(
            @Valid @RequestBody PresignedUrlRequest request
    ) {
        List<PresignedUrlInfo> response = propertyImagePresignService.issuePresignedUrls(request);
        return GlobalResponse.ok(response);
    }

    @SecurityNotRequired
    @Operation(summary = "매물 전체 목록 조회", description = "타입·경매 상태로 필터링하여 매물 목록을 조회합니다. 로그인 불필요.")
    @GetMapping
    public ResponseEntity<GlobalResponse<CustomSliceResponse<ReadAllPropertyResponse>>> getAllPropertyList(
            @RequestParam(required = false) PropertyType type,
            @RequestParam(name = "status", required = false) AuctionStatus auctionStatus,
            Pageable pageable
    ) {
        //TODO 전체 기능 구현 완료후 삭제 예정입니다.
        log.info("alloy loki test log");
        log.error("alloy loki error test");
        CustomSliceResponse<ReadAllPropertyResponse> response = propertyService.getAllPropertyList(type, auctionStatus, pageable);
        return GlobalResponse.ok(response);
    }

    @Operation(summary = "매물 키워드 검색", description = "주소·이름 등 키워드로 매물을 검색합니다. 로그인 불필요.")
    @GetMapping("/v1/search")
    public ResponseEntity<GlobalResponse<CustomSliceResponse<SearchPropertyResponse>>> search(
            @ModelAttribute SearchPropertyCond cond,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        CustomSliceResponse<SearchPropertyResponse> response = propertySearchService.search(cond, pageable);
        return GlobalResponse.ok(response);
    }

    @Operation(summary = "내 매물 목록 조회", description = "로그인한 판매자 본인의 매물 목록을 조회합니다. (SELLER 권한 필요)")
    @GetMapping("/v1/my")
    public ResponseEntity<GlobalResponse<CustomPageResponse<ReadAllPropertyResponse>>> getMyPropertyList(
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        CustomPageResponse<ReadAllPropertyResponse> response = propertyService.getMyPropertyList(authUser.getUserId(), pageable);
        return GlobalResponse.ok(response);
    }

    @SecurityNotRequired
    @Operation(summary = "매물 단건 조회", description = "매물 ID로 상세 정보를 조회합니다. 로그인 불필요.")
    @GetMapping("/v1/{propertyId}")
    public ResponseEntity<GlobalResponse<ReadPropertyResponse>> getProperty(@PathVariable Long propertyId) {
        ReadPropertyResponse response = propertyService.getProperty(propertyId);
        return GlobalResponse.ok(response);
    }

    @Operation(summary = "매물 수정", description = "매물 정보를 수정합니다. 본인 소유 매물만 가능합니다. (SELLER 권한 필요)")
    @PatchMapping("/v1/{propertyId}")
    public ResponseEntity<GlobalResponse<Void>> updateProperty(
            @Valid @RequestBody UpdatePropertyRequest request,
            @PathVariable Long propertyId,
            @AuthenticationPrincipal AuthUser authUser
    ) {

        propertyService.updateProperty(propertyId, request, authUser.getUserId());

        return GlobalResponse.noContent();
    }

    @Operation(summary = "매물 삭제", description = "매물을 삭제합니다. 진행 중인 경매가 있으면 삭제 불가합니다. (SELLER 권한 필요)")
    @DeleteMapping("/v1/{propertyId}")
    public ResponseEntity<GlobalResponse<Void>> deleteProperty(@PathVariable Long propertyId, @AuthenticationPrincipal AuthUser authUser) {

        propertyService.deleteProperty(propertyId, authUser.getUserId());

        return GlobalResponse.noContent();
    }

    @SecurityNotRequired
    @Operation(summary = "Elasticsearch 동기화", description = "전체 매물 데이터를 Elasticsearch에 동기화합니다. (관리자용)")
    @PostMapping("/v1/sync")
    public ResponseEntity<GlobalResponse<Void>> syncAllProperties() {

        propertySyncService.syncAllProperties();
        return GlobalResponse.noContent();
    }
}