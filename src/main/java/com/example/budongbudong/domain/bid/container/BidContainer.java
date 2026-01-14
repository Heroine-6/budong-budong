package com.example.budongbudong.domain.bid.container;

import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.bid.dto.CreateBidRequest;
import com.example.budongbudong.domain.bid.dto.CreateBidResponse;
import com.example.budongbudong.domain.bid.service.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/bids")
public class BidContainer {

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
}
