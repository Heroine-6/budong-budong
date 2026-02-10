package com.example.budongbudong.domain.bid.MQ;

import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.domain.bid.dto.request.CreateBidMessageRequest;
import com.example.budongbudong.domain.bid.dto.response.CreateBidResponse;
import com.example.budongbudong.domain.bid.service.BidService;
import com.example.budongbudong.domain.bid.config.BidMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BidConsumer {

    private final BidService bidService;

    /**
     * Queue에 발행된 입찰 메시지 구독
     */
    @RabbitListener(
            queues = BidMQConfig.BID_CREATE_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void receiveCreateBidMessage(CreateBidMessageRequest request) {
        log.info("[입찰] 메시지 수신 - auctionId={}, userId={}, bidPrice={}, bidAt={}",
                request.getAuctionId(), request.getUserId(), request.getCreateBidRequest().getPrice(), request.getBidAt());

        try {
            CreateBidResponse response = bidService.createBid(request.getCreateBidRequest(), request.getAuctionId(), request.getUserId());
            log.info("[입찰][입찰 생성] 완료 bidStatus={}", response.getBidStatus());

        } catch (CustomException e) {
            log.info("[입찰][입찰 생성] 처리 실패={}", e.getMessage());
        }
    }

}
