package com.example.budongbudong.domain.notification.event;

import com.example.budongbudong.domain.auction.event.AuctionClosedEvent;
import com.example.budongbudong.domain.auction.event.AuctionCreatedEvent;
import com.example.budongbudong.domain.auction.event.AuctionEndingSoonEvent;
import com.example.budongbudong.domain.auction.event.AuctionOpenEvent;
import com.example.budongbudong.domain.bid.event.BidCreatedEvent;
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

        CreateNotificationResponse response = notificationService.createSellerNotification(event.auctionId(), event.sellerId(), type);

        userNotificationService.createUserNotification(response.getId(), event.sellerId());
    }

    /**
     * 입찰 생성 이벤트 처리
     * - 새로운 입찰자 수신 등록
     * - 알림 수신에 동의한 판매자 + 입찰자 대상 알림
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendNotificationOnCreatedBid(BidCreatedEvent event) {

        NotificationDto dto = userNotificationService.createUserNotification(
                event.auctionId(),
                NotificationType.BID_UPDATE,
                event.bidderId()
        );

        sendNotification(dto);
    }

    /**
     * 경매 시작 이벤트 처리
     * - 알림 수신에 동의한 판매자 대상 알림
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendNotificationOnOpenAuction(AuctionOpenEvent event) {

        NotificationDto dto = notificationService.getNotification(event.auctionId(), NotificationType.AUCTION_START);

        sendNotification(dto);
    }

    /**
     * 경매 종료 임박 이벤트 처리
     * - 경매에 참여한 모든 입찰자 수신 등록
     * - 알림 수신에 동의한 판매자 + 입찰자 대상 알림
     */
    @TransactionalEventListener
    public void sendNotificationEndingSoonAuction(AuctionEndingSoonEvent event) {

        NotificationDto dto = userNotificationService.createUserNotificationAllBidders(event.auctionId(), NotificationType.AUCTION_END_SOON);

        sendNotification(dto);
    }

    /**
     * 경매 종료 이벤트 처리
     * - 경매에 참여한 모든 입찰자 수신 등록
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