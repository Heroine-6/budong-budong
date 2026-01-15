package com.example.budongbudong.domain.auction.controller;

import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.auction.dto.request.CreateAuctionRequest;
import com.example.budongbudong.domain.auction.dto.response.CreateAuctionResponse;
import com.example.budongbudong.domain.auction.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auctions")
public class AuctionController {

    private final AuctionService auctionService;

    @PostMapping
    public GlobalResponse<CreateAuctionResponse> createAuction(@RequestBody CreateAuctionRequest request) {

        CreateAuctionResponse result = auctionService.createAuction(request);

        return GlobalResponse.success(true, "경매 등록 성공", result);
    }
}
