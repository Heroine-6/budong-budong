package com.example.budongbudong.domain.bid.controller;

import com.example.budongbudong.common.dto.AuthUser;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.bid.dto.request.CreateBidRequest;
import com.example.budongbudong.domain.bid.dto.response.CreateBidMessageResponse;
import com.example.budongbudong.domain.bid.enums.BidStatus;
import com.example.budongbudong.domain.bid.service.BidRabbitMQService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/bids")
public class BidRabbitMQController {

    private final BidRabbitMQService bidRabbitMqService;

    /**
     * 입찰 등록 - 메시지 큐
     */
    @PostMapping("/auctions/{auctionId}")
    public ResponseEntity<GlobalResponse<CreateBidMessageResponse>> createBid(
            @Valid @RequestBody CreateBidRequest request,
            @PathVariable Long auctionId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        CreateBidMessageResponse response = bidRabbitMqService.publishBid(request, auctionId, authUser.getUserId());

        return response.getBidStatus().equals(BidStatus.REJECTED)
                ? GlobalResponse.okButRejected(response.getMessage())
                : GlobalResponse.created(response);
    }
}
