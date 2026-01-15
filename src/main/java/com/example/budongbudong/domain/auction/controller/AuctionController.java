package com.example.budongbudong.domain.auction.controller;

import com.example.budongbudong.common.dto.AuthUser;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.auction.dto.request.CreateAuctionRequest;
import com.example.budongbudong.domain.auction.dto.response.CancelAuctionResponse;
import com.example.budongbudong.domain.auction.dto.response.CreateAuctionResponse;
import com.example.budongbudong.domain.auction.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auctions")
public class AuctionController {

    private final AuctionService auctionService;

    @PostMapping
    public ResponseEntity<GlobalResponse<CreateAuctionResponse>> createAuction(@RequestBody CreateAuctionRequest request, @AuthenticationPrincipal AuthUser authUser) {

        CreateAuctionResponse response = auctionService.createAuction(request, authUser.getUserId());

        return ResponseEntity.ok(GlobalResponse.success(true, "경매 등록 성공", response));
    }

    @PatchMapping("/{auctionId}")
    public ResponseEntity<GlobalResponse<CancelAuctionResponse>> cancelAuction(@PathVariable Long auctionId, @AuthenticationPrincipal AuthUser authUser) {

        CancelAuctionResponse response = auctionService.cancelAuction(auctionId, authUser.getUserId());

        return ResponseEntity.ok(GlobalResponse.success(true, "경매 상태 변경 성공", response));
    }
}
