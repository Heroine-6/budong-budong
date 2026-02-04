package com.example.budongbudong.domain.auction.pubsub;

import com.example.budongbudong.common.entity.AuctionWinner;
import com.example.budongbudong.domain.auction.event.AuctionClosedEvent;
import com.example.budongbudong.domain.auction.event.DepositRefundEvent;
import com.example.budongbudong.domain.auctionwinner.repository.AuctionWinnerRepository;
import com.example.budongbudong.domain.bid.enums.BidStatus;
import com.example.budongbudong.domain.bid.repository.BidRepository;
import com.example.budongbudong.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Redis Pub/Sub을 통해 전달된 경매 종료 메세지를 수신하는 Subscriber
 * - 낙찰자 확정
 * - Bid 상태 변경
 * - 이미 낙찰자가 존재하면 즉시 종료
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionClosedSubscriber {
    private final AuctionWinnerRepository auctionWinnerRepository;
    private final BidRepository bidRepository;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void handleMessage(AuctionClosedEvent event) {

        Long auctionId = event.auctionId();
        log.info("[Redis-Sub] auction.closed auctionId={}", auctionId);

        if(auctionWinnerRepository.existsByAuctionId(auctionId)){
            log.warn("auctionId={} 이미 낙찰자가 존재합니다", auctionId);
            return;
        }
        bidRepository.findTopByAuctionIdOrderByPriceDescCreatedAtAsc(auctionId)
                .ifPresentOrElse(bid -> {
                    // 낙찰자 확정
                    auctionWinnerRepository.save(AuctionWinner.create(bid.getAuction(),bid.getUser(),bid.getPrice()));
                    bid.changeStatus(BidStatus.WON);

                    // 환불 대상 계산
                    List<Long> loserPaymentId = paymentRepository.findDepositPaymentIdsByAuctionIdAndNotWinnerUserId(
                            auctionId,
                            bid.getUser().getId()
                    );

                    // 환불 대상이 있을 때만 보증금 환불 이벤트 발행
                    if (!loserPaymentId.isEmpty()) {
                        eventPublisher.publishEvent(
                                new DepositRefundEvent(auctionId, bid.getUser().getId(), loserPaymentId)
                        );
                    }
                },
                ()-> log.info("auctionId={} 입찰이 없습니다",auctionId)
        );
    }
}
