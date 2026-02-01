package com.example.budongbudong.domain.payment.controller;

import com.example.budongbudong.common.dto.AuthUser;
import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.payment.dto.request.PaymentConfirmRequest;
import com.example.budongbudong.domain.payment.dto.request.PaymentRequest;
import com.example.budongbudong.domain.payment.dto.response.PaymentTossReadyResponse;
import com.example.budongbudong.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments/v2")
public class PaymentController {

    private final PaymentService paymentService;

    //TODO 구현 완료 후 AuthenticationPrincipal로 수정하기
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

}
