package com.example.budongbudong.domain.auction.controller;

import com.example.budongbudong.common.dto.AuthUser;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.auction.dto.request.CreateAuctionRequest;
import com.example.budongbudong.domain.auction.dto.request.CreateDutchAuctionRequest;
import com.example.budongbudong.domain.auction.dto.response.*;
import com.example.budongbudong.domain.auction.service.AuctionService;
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

    @PostMapping("/v1")
    public ResponseEntity<GlobalResponse<CreateAuctionResponse>> createAuction(
            @Valid @RequestBody CreateAuctionRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        CreateAuctionResponse response = auctionService.createAuction(request, authUser.getUserId());

        return GlobalResponse.ok(response);
    }

    @PatchMapping("/v1/{auctionId}")
    public ResponseEntity<GlobalResponse<CancelAuctionResponse>> cancelAuction(
            @PathVariable Long auctionId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        CancelAuctionResponse response = auctionService.cancelAuction(auctionId, authUser.getUserId());

        return GlobalResponse.ok(response);
    }

    @GetMapping("/v1/{auctionId}/info")
    public ResponseEntity<GlobalResponse<AuctionInfoResponse>> getAuctionInfo(@PathVariable Long auctionId) {

        AuctionInfoResponse response = auctionService.getAuctionInfo(auctionId);

        return GlobalResponse.ok(response);
    }

    @GetMapping("/v1/{auctionId}/statistics")
    public ResponseEntity<GlobalResponse<GetStatisticsResponse>> getAuctionStatistics(@PathVariable Long auctionId) {

        GetStatisticsResponse response = auctionService.getAuctionStatistics(auctionId);

        return GlobalResponse.ok(response);
    }

    @PostMapping("/v3/dutch")
    public ResponseEntity<GlobalResponse<CreateDutchAuctionResponse>> createDutchAuction(
            @Valid @RequestBody CreateDutchAuctionRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        CreateDutchAuctionResponse response = auctionService.createDutchAuction(request, authUser.getUserId());

        return GlobalResponse.created(response);
    }
}
