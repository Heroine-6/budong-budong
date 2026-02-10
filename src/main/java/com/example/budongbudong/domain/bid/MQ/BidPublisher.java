package com.example.budongbudong.domain.bid.MQ;

import com.example.budongbudong.domain.bid.config.BidMQConfig;
import com.example.budongbudong.domain.bid.dto.request.CreateBidMessage;
import com.example.budongbudong.domain.bid.dto.request.CreateBidRequest;
import com.example.budongbudong.domain.bid.dto.response.CreateBidMessageResponse;
import com.example.budongbudong.domain.bid.enums.BidStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BidPublisher {

    private static final long DEFAULT_DELAY_MILLIS = 30000L; // 30초
    private static final int MAX_RETRY_COUNT = 5; // 최대 재시도 횟수

    private final RabbitTemplate rabbitTemplate;

    /**
     * Queue 로 입찰 메세지 발행
     */
    public CreateBidMessageResponse publishBid(CreateBidRequest request, Long auctionId, Long userId) {
        log.info("[입찰] 생성 메시지 발행 | auctionId={}, userId={}, bidPrice={}", auctionId, userId, request.getPrice());

        CreateBidMessage message = CreateBidMessage.from(auctionId, userId, request);
        rabbitTemplate.convertAndSend(
                BidMQConfig.BID_EXCHANGE,
                BidMQConfig.BID_CREATE_KEY,
                message
        );

        return CreateBidMessageResponse.from(BidStatus.PLACED, "입찰 요청 완료");
    }

    /**
     * Delay Queue 로 재시도 메시지 발행
     */
    public void publishRetry(CreateBidMessage message) {

        int retryCount = message.getRetryCount();
        if (retryCount >= MAX_RETRY_COUNT) {
            log.error("[입찰] 최대 재시도 초과 - auctionId={}, userId={}, retryCount={}",
                    message.getAuctionId(), message.getUserId(), message.getRetryCount());

            rabbitTemplate.convertAndSend(BidMQConfig.BID_DLQ, message);

            return;
        }

        message.incrementRetryCount();
        long delayMillis = (long) Math.pow(2, retryCount) * DEFAULT_DELAY_MILLIS;

        log.info("[입찰] 재시도 메시지 발행 | auctionId={}, userId={}, retryCount={}",
                message.getAuctionId(), message.getUserId(), message.getRetryCount());

        rabbitTemplate.convertAndSend(
                BidMQConfig.BID_DELAY_QUEUE,
                message,
                m -> {
                    m.getMessageProperties().setExpiration(String.valueOf(delayMillis));
                    return m;
                }
        );
    }

}

