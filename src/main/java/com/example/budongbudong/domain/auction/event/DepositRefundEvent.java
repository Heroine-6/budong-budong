package com.example.budongbudong.domain.auction.event;

import java.util.List;

/**
 * 경매 종료 이후 낙찰자가 확정되고
 * 후속 처리를 진행해도 되는 시점을 알리는 이벤트
 */
public record DepositRefundEvent(
        Long auctionId,
        Long winnerUserId,
        List<Long> loserDepositPaymentIds
) {
}

