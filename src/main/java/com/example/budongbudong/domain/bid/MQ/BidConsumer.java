package com.example.budongbudong.domain.bid.MQ;

import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.domain.bid.config.BidMQConfig;
import com.example.budongbudong.domain.bid.dto.request.CreateBidMessage;
import com.example.budongbudong.domain.bid.dto.response.CreateBidResponse;
import com.example.budongbudong.domain.bid.service.BidService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BidConsumer {

    private static final int MAX_RETRY_COUNT = 5;

    private final BidService bidService;
    private final BidPublisher bidPublisher;

    @RabbitListener(
            queues = BidMQConfig.BID_CREATE_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void receiveCreateBidMessage(CreateBidMessage request) {
        log.info("[입찰] 메시지 수신 - auctionId={}, userId={}, bidPrice={}, retryCount={}",
                request.getAuctionId(), request.getUserId(), request.getCreateBidRequest().getPrice(), request.getRetryCount());

        try {
            CreateBidResponse response = bidService.createBid(
                    request.getCreateBidRequest(),
                    request.getAuctionId(),
                    request.getUserId()
            );

            log.info("[입찰] 처리 완료 - auctionId={}, bidId={}, bidStatus={}, retryCount={}",
                    response.getAuctionId(), response.getBidId(), response.getBidStatus(), request.getRetryCount());

        } catch (CustomException e) {
            log.info("[입찰] 처리 실패 - auctionId={}, error={}", request.getAuctionId(), e.getMessage());

        } catch (Exception e) {
            log.info("[입찰] 처리 실패 - auctionId={}, error={}", request.getAuctionId(), e.getMessage());

            bidPublisher.publishRetry(request);
        }
    }
}
