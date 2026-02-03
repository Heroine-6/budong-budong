package com.example.budongbudong.domain.notification.event;

import com.example.budongbudong.domain.auction.event.AuctionClosedEvent;
import com.example.budongbudong.domain.notification.dto.CreateNotificationResponse;
import com.example.budongbudong.domain.notification.dto.NotificationDto;
import com.example.budongbudong.domain.notification.enums.NotificationType;
import com.example.budongbudong.domain.notification.service.NotificationService;
import com.example.budongbudong.domain.notification.usernotification.dto.GetNotificationTargetResponse;
import com.example.budongbudong.domain.notification.usernotification.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

/**
 * 알림(Notification) 처리를 담당하는 이벤트 리스너
 * - 도메인 이벤트 발생 이후 알림 생성 및 전송 처리
 * - 모든 이벤트는 트랜잭션 커밋 이후(AFTER_COMMIT)에 처리됨
 * - 비즈니스 로직과 알림 로직을 분리하기 위한 구성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final UserNotificationService userNotificationService;

    /**
     * 경매 생성 이벤트 처리
     * - 판매자 대상 알림 정의 생성
     * - 경매 전 과정에서 사용할 알림 유형 미리 등록
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void createNotificationOnCreatedAuction(AuctionCreatedEvent event) {

        List<NotificationType> types = List.of(
                NotificationType.AUCTION_START,
                NotificationType.AUCTION_END_SOON,
                NotificationType.AUCTION_END,
                NotificationType.BID_UPDATE
        );

        types.forEach(type -> createSellerNotification(event, type));
    }

    private void createSellerNotification(AuctionCreatedEvent event, NotificationType type) {

        CreateNotificationResponse response = notificationService.createSellerNotification(event.getAuctionId(), event.getSellerId(), type);

        userNotificationService.createUserNotification(response.getId(), event.getSellerId());
    }

    /**
     * 입찰 생성 이벤트 처리
     * - 알림 수신에 동의한 판매자 + 입찰자 대상 알림
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendNotificationOnCreatedBid(BidCreatedEvent event) {

        NotificationDto dto = userNotificationService.createUserNotification(event.getAuctionId(), event.getType(), event.getBidderId());

        sendNotification(dto);
    }

    /**
     * 경매 시작 이벤트 처리
     * - 알림 수신에 동의한 판매자 대상 알림
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendNotificationOnOpenAuction(AuctionOpenEvent event) {

        NotificationDto dto = notificationService.getNotification(event.getAuctionId(), event.getType());

        sendNotification(dto);
    }

    /**
     * 경매 종료 이벤트 처리
     * - 알림 수신에 동의한 판매자 + 입찰자 대상 알림
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendNotificationOnClosedAuction(AuctionClosedEvent event) {

        NotificationDto dto = userNotificationService.createUserNotificationAllBidders(event.auctionId(), NotificationType.AUCTION_END);

        sendNotification(dto);
    }

    private void sendNotification(NotificationDto dto) {

        List<GetNotificationTargetResponse> targets = userNotificationService.getNotificationTargets(dto.getNotificationId());

        targets.forEach(target ->
                notificationService.sendMessage(target.getUserId(), dto.getContent())
        );
    }
}