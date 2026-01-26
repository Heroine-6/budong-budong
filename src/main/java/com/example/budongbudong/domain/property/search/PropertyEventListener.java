package com.example.budongbudong.domain.property.search;

import com.example.budongbudong.domain.property.event.PropertyChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 도메인 이벤트를 받아 검색 인덱싱으로 연결하는 리스너
 */
@Component
@RequiredArgsConstructor
public class PropertyEventListener {

    private final PropertyIndexer propertyIndexer;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PropertyChangedEvent event) {
        switch(event.type()) {
            case CREATED,UPDATED -> propertyIndexer.insertUpdate(event.propertyId());
            case DELETED -> propertyIndexer.delete(event.propertyId());
        }
    }
}
