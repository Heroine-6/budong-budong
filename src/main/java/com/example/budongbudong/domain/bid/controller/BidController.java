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
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<GlobalResponse<CreateBidResponse>> createBid(@Valid @RequestBody CreateBidRequest request, @PathVariable Long auctionId) {

        CreateBidResponse response = bidService.createBid(request, auctionId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(GlobalResponse.success(
                        true,
                        "입찰이 성공적으로 등록되었습니다.",
                        response
                ));
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

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(GlobalResponse.success(
                        true,
                        "입찰 내역이 성공적으로 조회되었습니다.",
                        response
                ));
    }

    /**
     * 내 입찰 내역 조회
     */
    @GetMapping("/my/{userId}")
    public ResponseEntity<GlobalResponse<CustomPageResponse<ReadMyBidsResponse>>> readMyBids(
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        Page<ReadMyBidsResponse> page = bidService.readMyBids(authUser.getUserId(), pageable);

        CustomPageResponse<ReadMyBidsResponse> response = CustomPageResponse.from(page);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(GlobalResponse.success(
                        true,
                        "내 입찰 내역이 성공적으로 조회되었습니다.",
                        response
                ));
    }
}
