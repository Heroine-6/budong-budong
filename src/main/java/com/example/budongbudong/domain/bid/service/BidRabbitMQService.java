package com.example.budongbudong.domain.bid.service;

import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.bid.dto.request.CreateBidMessageRequest;
import com.example.budongbudong.domain.bid.dto.request.CreateBidRequest;
import com.example.budongbudong.domain.bid.dto.response.CreateBidMessageResponse;
import com.example.budongbudong.domain.bid.enums.BidStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidRabbitMQService {

    private final AuctionRepository auctionRepository;
    private final RedissonClient redissonClient;
    private final BidTxService bidTxService;
    private final RabbitTemplate rabbitTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;
    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    /**
     * Queue 로 입찰 메세지 발행
     * 발행 전 입찰가 검증
     **/
    public CreateBidMessageResponse publishBid(CreateBidRequest request, Long auctionId, Long userId) {

        Long inputPrice = request.getPrice();

        // 입찰 최고가 캐싱
        String AUCTION_HIGHEST_BID_KEY = "auction:" + auctionId + ":max-price";
        String maxPrice = redisTemplate.opsForValue().get(AUCTION_HIGHEST_BID_KEY);

        if (maxPrice != null && inputPrice <= Long.parseLong(maxPrice)) {
            return CreateBidMessageResponse.from(BidStatus.REJECTED, "입찰가는 현재 최고가보다 높아야 합니다.");
        }

        // TODO: 최소입찰단위 검증
        // 검증에 필요한 경매 시작가와 입찰 최소단위 캐싱

        redisTemplate.opsForValue().set(AUCTION_HIGHEST_BID_KEY, inputPrice.toString());
        CreateBidMessageRequest messageRequest = CreateBidMessageRequest.from(auctionId, userId, request);
        rabbitTemplate.convertAndSend(exchangeName, routingKey, messageRequest);

        return CreateBidMessageResponse.from(BidStatus.PLACED, "입찰 요청 완료");
    }

    /**
     * Queue에 발행된 메시지 구독
     * 입찰 등록 - Lock + 메시지큐
     */
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void createBid(CreateBidMessageRequest request) {

        Long userId = request.getUserId();
        Long auctionId = request.getAuctionId();
        CreateBidRequest createBidRequest = request.getCreateBidRequest();

        long t0 = System.currentTimeMillis();
        String th = Thread.currentThread().getName();

        LocalDateTime endedAt = auctionRepository.getEndedAtOrThrow(auctionId);
        LocalDateTime now = LocalDateTime.now();

        boolean lastHour = !now.isBefore(endedAt.minusHours(1));

        long waitTime = lastHour ? 2L : 0L;

        String lockKey = "lock:auction:" + auctionId;
        RLock lock = redissonClient.getFairLock(lockKey);

        boolean acquired = false;

        try {
            log.info("[{}] t={} TRY_LOCK auctionId={}", th, System.currentTimeMillis(), auctionId);

            acquired = lock.tryLock(waitTime, -1, TimeUnit.SECONDS);

            if (!acquired) {
                log.info("[{}] LOCK_FAILED auctionId={} waited={}ms", th, auctionId, System.currentTimeMillis() - t0);
                throw new CustomException(ErrorCode.BID_LOCK_TIMEOUT);
            }

            log.info("[{}] LOCK_ACQUIRED auctionId={} waited={}ms", th, auctionId, System.currentTimeMillis() - t0);

            bidTxService.createBidTx(createBidRequest, auctionId, userId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.BID_LOCK_FAILED);

        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
