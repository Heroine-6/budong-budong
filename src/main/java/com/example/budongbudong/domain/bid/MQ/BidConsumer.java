package com.example.budongbudong.domain.bid.MQ;

import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.domain.bid.config.BidMQConfig;
import com.example.budongbudong.domain.bid.dto.request.CreateBidMessageRequest;
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
    public void receiveCreateBidMessage(CreateBidMessageRequest request) {
        log.info("[입찰] 메시지 수신 - auctionId={}, userId={}, bidPrice={}, retryCount={}",
                request.getAuctionId(), request.getUserId(),
                request.getCreateBidRequest().getPrice(), request.getRetryCount());

        try {
            CreateBidResponse response = bidService.createBid(
                    request.getCreateBidRequest(), request.getAuctionId(), request.getUserId());
            log.info("[입찰] 처리 완료 - auctionId={}, bidId={}, bidStatus={}, retryCount={}",
                    response.getAuctionId(), response.getBidId(), response.getBidStatus(), request.getRetryCount());

        } catch (CustomException e) {
            log.info("[입찰] 처리 실패 - auctionId={}, error={}", request.getAuctionId(), e.getMessage());

        } catch (Exception e) {
            handleRetry(request, e);
        }
    }

    private void handleRetry(CreateBidMessageRequest request, Exception e) {
        request.incrementRetryCount();

        if (request.getRetryCount() > MAX_RETRY_COUNT) {
            log.error("[입찰] 최대 재시도 초과 - auctionId={}, userId={}, retryCount={}",
                    request.getAuctionId(), request.getUserId(), request.getRetryCount());
            return;
        }

        log.warn("[입찰] 재시도 예약 - auctionId={}, retryCount={}/{}, error={}",
                request.getAuctionId(), request.getRetryCount(), MAX_RETRY_COUNT, e.getMessage());

        bidPublisher.publishRetry(request);
    }
}
