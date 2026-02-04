package com.example.budongbudong.domain.payment.utils;

import com.example.budongbudong.domain.auction.event.DepositRefundEvent;
import com.example.budongbudong.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 경매 종료 후 발생한 환불 이벤트를 처리하는 핸들러
 * - 탈락자 보증금 결제에 대해 환불 요청을 위임한다
 */
@Component
@RequiredArgsConstructor
public class AuctionClosedEventHandler {

    private final PaymentService paymentService;

    /* 탈락자 결제 ID 목록에 대해 환불 요청을 수행한다 */
    @EventListener
    public void handle(DepositRefundEvent event) {

        event.loserDepositPaymentIds()
                .forEach(paymentService::requestRefund);
    }
}
