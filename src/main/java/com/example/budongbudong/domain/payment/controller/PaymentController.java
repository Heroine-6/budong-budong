package com.example.budongbudong.domain.payment.controller;

import com.example.budongbudong.common.dto.AuthUser;
import com.example.budongbudong.common.response.CustomSliceResponse;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.payment.dto.request.PaymentConfirmRequest;
import com.example.budongbudong.domain.payment.dto.request.PaymentRequest;
import com.example.budongbudong.domain.payment.dto.response.*;
import com.example.budongbudong.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments/v2")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/auctions/{auctionId}")
    public ResponseEntity<GlobalResponse<PaymentTossReadyResponse>> requestPayment(
            @PathVariable Long auctionId,
            @RequestBody PaymentRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        PaymentTossReadyResponse response = paymentService.requestPayment(authUser.getUserId(), auctionId, request.type());
        return GlobalResponse.ok(response);
    }

    @PostMapping("/confirm")
        public ResponseEntity<GlobalResponse<Void>>  confirmPayment(@RequestBody PaymentConfirmRequest request) {
        paymentService.confirmPayment(request);
        return GlobalResponse.noContent();
    }

    @GetMapping
    public ResponseEntity<GlobalResponse<CustomSliceResponse<ReadAllPaymentResponse>>> getAllPaymentList(
            @AuthenticationPrincipal AuthUser authUser,
            @PageableDefault Pageable pageable
    ) {
        CustomSliceResponse<ReadAllPaymentResponse> slice = paymentService.getAllPaymentList(authUser.getUserId(), pageable);
        return GlobalResponse.ok(slice);
    }

   @GetMapping("/{paymentId}")
    public ResponseEntity<GlobalResponse<ReadPaymentResponse>> getPaymentDetail(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long paymentId
   ) {
       ReadPaymentResponse response= paymentService.getPaymentDetail(authUser.getUserId(), paymentId);
        return GlobalResponse.ok(response);
   }

}
