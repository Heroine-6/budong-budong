package com.example.budongbudong.domain.auction.pubsub;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

/**
 * Redis Pub/Sub Subscriber 설정
 * - Redis 채널과 Subscriber 연결
 * - 메시지 수신 시 handleMessage 호출
 */
@Configuration
@RequiredArgsConstructor
public class RedisSubscriberConfig {

    private final AuctionClosedSubscriber auctionClosedSubscriber;

    @Bean
    public MessageListenerAdapter auctionClosedListener(){
        MessageListenerAdapter adapter = new MessageListenerAdapter(auctionClosedSubscriber, "handleMessage");
        adapter.setSerializer(new GenericJackson2JsonRedisSerializer());
        return adapter;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter auctionClosedListener,
            ChannelTopic auctionClosedTopic
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(auctionClosedListener, auctionClosedTopic);
        return container;
    }
}
