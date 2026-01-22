package com.example.budongbudong.domain.auction.event;

import com.example.budongbudong.domain.auction.pubsub.AuctionClosedPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 경매 종료 도메인 이벤트를 수신해 Redis Pub/Sub으로 외부에 전달하는 리스너
 * - DB 트랜잭션 커밋 이후(AFTER_COMMIT)에만 실행
 * - DB 상태와 메세지 불일치 방지
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionClosedEventListener {

    private final AuctionClosedPublisher auctionClosedPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAuctionClosedEvent(AuctionClosedEvent event) {
        auctionClosedPublisher.publish(event.auctionId());
    }
}
