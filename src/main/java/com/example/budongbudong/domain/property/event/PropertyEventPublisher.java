package com.example.budongbudong.domain.property.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 이벤트 발행 책임 컴포넌트
 */
@Component
@RequiredArgsConstructor
public class PropertyEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publish(Long id, PropertyEventType type){
        this.publisher.publishEvent(new PropertyChangedEvent(id, type));
    }
}
