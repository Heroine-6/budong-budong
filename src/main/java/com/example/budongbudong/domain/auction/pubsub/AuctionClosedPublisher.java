package com.example.budongbudong.domain.auction.pubsub;

import com.example.budongbudong.domain.auction.event.AuctionClosedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

/**
 * Redis Pub/Sub을 통해 경매 종료 이벤트를 발행하는 Publisher
 * - Redis channel에 메세지 전달
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionClosedPublisher {

    private final RedisTemplate<String, Object> pubSubRedisTemplate;
    private final ChannelTopic auctionClosedTopic;

    public void publish(Long auctionId) {

        pubSubRedisTemplate.convertAndSend(auctionClosedTopic.getTopic(), new AuctionClosedEvent(auctionId));
        log.info("[Redis-Pub] auction.closed -auctionId:{}", auctionId);
    }
}
