package com.example.budongbudong.domain.auction.controller;

import com.example.budongbudong.common.dto.AuthUser;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.auction.dto.request.CreateAuctionRequest;
import com.example.budongbudong.domain.auction.dto.request.CreateDutchAuctionRequest;
import com.example.budongbudong.domain.auction.dto.response.*;
import com.example.budongbudong.domain.auction.service.AuctionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "경매")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auctions")
public class AuctionController {

    private final AuctionService auctionService;

    @Operation(summary = "일반 경매 등록", description = "매물에 일반(영국식) 경매를 등록합니다. 시작일은 현재 이후, 최대 7일까지 가능합니다. (SELLER 권한 필요)")
    @PostMapping("/v1")
    public ResponseEntity<GlobalResponse<CreateAuctionResponse>> createAuction(
            @Valid @RequestBody CreateAuctionRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        CreateAuctionResponse response = auctionService.createAuction(request, authUser.getUserId());

        return GlobalResponse.ok(response);
    }

    @Operation(summary = "경매 취소", description = "SCHEDULED 상태의 경매를 취소합니다. 이미 시작된 경매는 취소 불가합니다. (SELLER 권한 필요)")
    @PatchMapping("/v1/{auctionId}")
    public ResponseEntity<GlobalResponse<CancelAuctionResponse>> cancelAuction(
            @PathVariable Long auctionId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        CancelAuctionResponse response = auctionService.cancelAuction(auctionId, authUser.getUserId());

        return GlobalResponse.ok(response);
    }

    @Operation(summary = "경매 입찰 정보 조회", description = "현재 최고 입찰가와 총 입찰자 수를 조회합니다. 로그인 불필요.")
    @GetMapping("/v1/{auctionId}/info")
    public ResponseEntity<GlobalResponse<AuctionInfoResponse>> getAuctionInfo(@PathVariable Long auctionId) {

        AuctionInfoResponse response = auctionService.getAuctionInfo(auctionId);

        return GlobalResponse.ok(response);
    }

    @Operation(summary = "경매 경쟁 통계 조회", description = "총 입찰자 수, 입찰 횟수, 가격 상승 금액, 최근 입찰 시각 등 경쟁 통계를 조회합니다. 로그인 불필요.")
    @GetMapping("/v1/{auctionId}/statistics")
    public ResponseEntity<GlobalResponse<GetStatisticsResponse>> getAuctionStatistics(@PathVariable Long auctionId) {

        GetStatisticsResponse response = auctionService.getAuctionStatistics(auctionId);

        return GlobalResponse.ok(response);
    }

    @Operation(summary = "네덜란드식 경매 등록", description = "시간이 지날수록 가격이 내려가는 네덜란드식 경매를 등록합니다. 시작일은 익일부터 가능합니다. (SELLER 권한 필요)")
    @PostMapping("/v3/dutch")
    public ResponseEntity<GlobalResponse<CreateDutchAuctionResponse>> createDutchAuction(
            @Valid @RequestBody CreateDutchAuctionRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        CreateDutchAuctionResponse response = auctionService.createDutchAuction(request, authUser.getUserId());

        return GlobalResponse.created(response);
    }
}
