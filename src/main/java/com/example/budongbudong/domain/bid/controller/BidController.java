package com.example.budongbudong.domain.bid.controller;

import com.example.budongbudong.common.dto.AuthUser;
import com.example.budongbudong.common.response.CustomPageResponse;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.common.utils.annotation.SecurityNotRequired;
import com.example.budongbudong.domain.bid.MQ.BidPublisher;
import com.example.budongbudong.domain.bid.dto.request.CreateBidRequest;
import com.example.budongbudong.domain.bid.dto.response.CreateBidMessageResponse;
import com.example.budongbudong.domain.bid.dto.response.CreateBidResponse;
import com.example.budongbudong.domain.bid.dto.response.ReadAllBidsResponse;
import com.example.budongbudong.domain.bid.dto.response.ReadMyBidsResponse;
import com.example.budongbudong.domain.bid.enums.BidStatus;
import com.example.budongbudong.domain.bid.service.BidService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "입찰")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bids")
public class BidController {

    private final BidService bidService;
    private final BidPublisher bidPublisher;

    @Operation(summary = "일반 입찰", description = "경매에 직접 입찰합니다. 현재 최고가보다 높아야 하며 최소 입찰 단위를 준수해야 합니다. (GENERAL 권한 필요)")
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

    @SecurityNotRequired
    @Operation(summary = "입찰 내역 조회", description = "해당 경매의 전체 입찰 내역을 최신순으로 조회합니다. 로그인 불필요.")
    @GetMapping("/v1/auctions/{auctionId}")
    public ResponseEntity<GlobalResponse<CustomPageResponse<ReadAllBidsResponse>>> readAllBids(
            @PathVariable Long auctionId,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ReadAllBidsResponse> page = bidService.readAllBids(auctionId, pageable);
        CustomPageResponse<ReadAllBidsResponse> response = CustomPageResponse.from(page);
        return GlobalResponse.ok(response);
    }

    @Operation(summary = "내 입찰 내역 조회", description = "로그인한 사용자의 입찰 내역을 조회합니다. status 파라미터로 WINNING·OUTBID·REJECTED 필터링 가능합니다. (GENERAL 권한 필요)")
    @GetMapping("/v1/my")
    public ResponseEntity<GlobalResponse<CustomPageResponse<ReadMyBidsResponse>>> readMyBids(
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String status
    ) {
        CustomPageResponse<ReadMyBidsResponse> response = bidService.readMyBids(authUser.getUserId(), status, pageable);
        return GlobalResponse.ok(response);
    }

    @Operation(summary = "일반 입찰 (메시지 큐)", description = "RabbitMQ를 통해 비동기로 입찰을 처리합니다. 트래픽이 많을 때 권장합니다. (GENERAL 권한 필요)")
    @PostMapping("/v2/auctions/{auctionId}")
    public ResponseEntity<GlobalResponse<CreateBidMessageResponse>> publishBid(
            @Valid @RequestBody CreateBidRequest request,
            @PathVariable Long auctionId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        CreateBidMessageResponse response = bidPublisher.publishBid(request, auctionId, authUser.getUserId());
        return response.getBidStatus().equals(BidStatus.REJECTED)
                ? GlobalResponse.okButRejected(response.getMessage())
                : GlobalResponse.created(response);
    }

    @Operation(summary = "네덜란드식 경매 입찰", description = "현재 시각 기준으로 계산된 현재가로 즉시 낙찰됩니다. 첫 번째 입찰자가 낙찰되며 경매가 즉시 종료됩니다. (GENERAL 권한 필요)")
    @PostMapping("/v3/auctions/dutch/{auctionId}")
    public ResponseEntity<GlobalResponse<CreateBidResponse>> createDutchBid(
            @PathVariable Long auctionId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        CreateBidResponse response = bidService.createDutchBid(auctionId, authUser.getUserId());
        return GlobalResponse.created(response);
    }
}