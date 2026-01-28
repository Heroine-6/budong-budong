package com.example.budongbudong.domain.bid.controller;

import com.example.budongbudong.common.dto.AuthUser;
import com.example.budongbudong.common.response.CustomPageResponse;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.bid.dto.request.CreateBidRequest;
import com.example.budongbudong.domain.bid.dto.response.CreateBidMessageResponse;
import com.example.budongbudong.domain.bid.dto.response.CreateBidResponse;
import com.example.budongbudong.domain.bid.dto.response.ReadAllBidsResponse;
import com.example.budongbudong.domain.bid.dto.response.ReadMyBidsResponse;
import com.example.budongbudong.domain.bid.enums.BidStatus;
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
@RequestMapping("/api/bids")
public class BidController {

    private final BidService bidService;

    /**
     * 입찰 등록
     */
    @PostMapping("/v1/auctions/{auctionId}")
    public ResponseEntity<GlobalResponse<CreateBidResponse>> createBid(
            @Valid @RequestBody CreateBidRequest request,
            @PathVariable Long auctionId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        CreateBidResponse response = bidService.createBid(request, auctionId, authUser.getUserId());

        return response.getBidStatus().equals(BidStatus.REJECTED)
                ? GlobalResponse.okButRejected(response.getMessage())
                : GlobalResponse.created(response);
    }

    /**
     * 입찰 내역 조회
     */
    @GetMapping("/v1/auctions/{auctionId}")
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
    @GetMapping("/v1/my")
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

    /**
     * 입찰 등록 - 메시지 큐
     */
    @PostMapping("/v2/auctions/{auctionId}")
    public ResponseEntity<GlobalResponse<CreateBidMessageResponse>> publishBid(
            @Valid @RequestBody CreateBidRequest request,
            @PathVariable Long auctionId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        CreateBidMessageResponse response = bidService.publishBid(request, auctionId, authUser.getUserId());

        return response.getBidStatus().equals(BidStatus.REJECTED)
                ? GlobalResponse.okButRejected(response.getMessage())
                : GlobalResponse.created(response);
    }
}
