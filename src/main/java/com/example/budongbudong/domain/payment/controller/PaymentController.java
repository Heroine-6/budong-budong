package com.example.budongbudong.domain.payment.controller;

import com.example.budongbudong.common.dto.AuthUser;
import com.example.budongbudong.common.response.CustomSliceResponse;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.payment.dto.request.PaymentConfirmRequest;
import com.example.budongbudong.domain.payment.dto.request.PaymentRequest;
import com.example.budongbudong.domain.payment.dto.response.*;
import com.example.budongbudong.domain.payment.enums.PaymentType;
import com.example.budongbudong.domain.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "결제")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments/v2")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 요청", description = "낙찰된 경매에 대해 토스페이먼츠 결제를 요청합니다. 응답의 결제 URL로 리다이렉트하세요.")
    @PostMapping("/auctions/{auctionId}")
    public ResponseEntity<GlobalResponse<PaymentTossReadyResponse>> requestPayment(
            @PathVariable Long auctionId,
            @RequestBody PaymentRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        PaymentTossReadyResponse response = paymentService.requestPayment(authUser.getUserId(), auctionId, request.type());
        return GlobalResponse.ok(response);
    }

    @Operation(summary = "결제 승인", description = "토스페이먼츠에서 리다이렉트된 후 결제를 최종 승인합니다. paymentKey·orderId·amount가 필요합니다.")
    @PostMapping("/confirm")
    public ResponseEntity<GlobalResponse<Void>> confirmPayment(
            @Valid @RequestBody PaymentConfirmRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        paymentService.confirmPayment(authUser.getUserId(), request);
        return GlobalResponse.noContent();
    }

    @Operation(summary = "내 결제 목록 조회", description = "로그인한 사용자의 결제 내역을 최신순으로 조회합니다.")
    @GetMapping
    public ResponseEntity<GlobalResponse<CustomSliceResponse<ReadAllPaymentResponse>>> getAllPaymentList(
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault Pageable pageable
    ) {
        CustomSliceResponse<ReadAllPaymentResponse> slice = paymentService.getAllPaymentList(authUser.getUserId(), pageable);
        return GlobalResponse.ok(slice);
    }

    @Operation(summary = "결제 단건 조회", description = "결제 ID로 상세 결제 정보를 조회합니다.")
    @GetMapping("/{paymentId}")
    public ResponseEntity<GlobalResponse<ReadPaymentResponse>> getPaymentDetail(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long paymentId
    ) {
        ReadPaymentResponse response = paymentService.getPaymentDetail(authUser.getUserId(), paymentId);
        return GlobalResponse.ok(response);
    }

    /**
     * 재시도 까지 실패 후 사용자 요청 환불
     */
    @Operation(summary = "환불 요청", description = "자동 환불 재시도가 모두 실패한 경우 사용자가 직접 환불을 요청합니다.")
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<GlobalResponse<Void>> requestRefundByUser(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long paymentId
    ) {
        paymentService.requestRefundByUser(authUser.getUserId(), paymentId);
        return GlobalResponse.noContent();
    }

    @GetMapping("/auctions/{auctionId}/info")
    public ResponseEntity<GlobalResponse<PaymentInfoResponse>> getPaymentInfo(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long auctionId,
            @RequestParam PaymentType type
    ) {

        PaymentInfoResponse response = paymentService.getPaymentInfo(auctionId, authUser.getUserId(), type);

        return GlobalResponse.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<GlobalResponse<List<MyPaymentListResponse>>> getMyRequiredPaymentsByType(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam PaymentType type
    ) {
        return GlobalResponse.ok(
                paymentService.getMyRequiredPaymentsByType(authUser.getUserId(), type)
        );
    }
}
