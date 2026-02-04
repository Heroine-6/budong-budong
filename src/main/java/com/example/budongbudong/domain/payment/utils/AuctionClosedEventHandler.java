package com.example.budongbudong.domain.payment.utils;

import com.example.budongbudong.domain.auction.event.DepositRefundEvent;
import com.example.budongbudong.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionClosedEventHandler {

    private final PaymentService paymentService;

    @EventListener
    public void handle(DepositRefundEvent event) {

        event.loserDepositPaymentIds()
                .forEach(paymentService::requestRefund);
    }
}
