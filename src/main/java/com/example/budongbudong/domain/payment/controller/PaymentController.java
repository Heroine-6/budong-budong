package com.example.budongbudong.domain.payment.controller;

import com.example.budongbudong.common.response.GlobalResponse;
import com.example.budongbudong.domain.payment.dto.request.PaymentConfirmRequest;
import com.example.budongbudong.domain.payment.dto.request.PaymentRequest;
import com.example.budongbudong.domain.payment.dto.response.PaymentRequestResponse;
import com.example.budongbudong.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/payments")
public class PaymentController {

    private final PaymentService paymentService;

    //TODO 구현 완료 후 AuthenticationPrincipal로 수정하기
    @PostMapping("/{auctionId}")
    public ResponseEntity<GlobalResponse<PaymentRequestResponse>> requestPayment(
            @PathVariable Long auctionId,
            @RequestBody PaymentRequest request
    ) {
        PaymentRequestResponse response = paymentService.requestPayment(1L, auctionId, request.type());
        return GlobalResponse.ok(response);
    }

    @PostMapping("/confirm")
        public ResponseEntity<GlobalResponse<Void>>  confirmPayment(@RequestBody PaymentConfirmRequest request) {
        paymentService.confirmPayment(request);
        return GlobalResponse.noContent();
    }

}
