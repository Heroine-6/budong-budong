package com.example.budongbudong.domain.bid.controller;

import com.example.budongbudong.common.dto.AuthUser;
import com.example.budongbudong.common.response.CustomPageResponse;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.bid.dto.request.CreateBidRequest;
import com.example.budongbudong.domain.bid.dto.response.CreateBidResponse;
import com.example.budongbudong.domain.bid.dto.response.ReadAllBidsResponse;
import com.example.budongbudong.domain.bid.dto.response.ReadMyBidsResponse;
import com.example.budongbudong.domain.bid.service.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bids")
public class BidController {

    private final BidService bidService;

    /**
     * 입찰 등록
     */
    @PostMapping("/auctions/{auctionId}")
    public ResponseEntity<GlobalResponse<CreateBidResponse>> createBid(
            @Valid @RequestBody CreateBidRequest request,
            @PathVariable Long auctionId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        CreateBidResponse response = bidService.createBid(request, auctionId, authUser.getUserId());

        return GlobalResponse.created(response);
    }

    /**
     * 입찰 내역 조회
     */
    @GetMapping("/auctions/{auctionId}")
    public ResponseEntity<GlobalResponse<CustomPageResponse<ReadAllBidsResponse>>> readAllBids(
            @PathVariable Long auctionId,
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        Page<ReadAllBidsResponse> page = bidService.readAllBids(auctionId, pageable);

        CustomPageResponse<ReadAllBidsResponse> response = CustomPageResponse.from(page);

        return GlobalResponse.ok(response);
    }

    /**
     * 내 입찰 내역 조회
     */
    @GetMapping("/my")
    public ResponseEntity<GlobalResponse<CustomPageResponse<ReadMyBidsResponse>>> readMyBids(
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable,
            @RequestParam(required = false) String status
    ) {
        CustomPageResponse<ReadMyBidsResponse> response = bidService.readMyBids(authUser.getUserId(), status, pageable);

        return GlobalResponse.ok(response);
    }
}
