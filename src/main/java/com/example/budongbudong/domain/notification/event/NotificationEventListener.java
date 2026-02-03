package com.example.budongbudong.domain.notification.event;

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
     * CreatedAuctionEvent 발생 시
     * 판매자 수신 알림(Notification) 생성
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void createNotificationOnCreatedAuction(CreatedAuctionEvent event) {

        List<NotificationType> types = List.of(
                NotificationType.AUCTION_START,
                NotificationType.AUCTION_END_SOON,
                NotificationType.AUCTION_END,
                NotificationType.BID_UPDATE
        );

        types.forEach(type -> createSellerNotification(event, type));
    }

    private void createSellerNotification(CreatedAuctionEvent event, NotificationType type) {

        CreateNotificationResponse response = notificationService.createSellerNotification(event.getAuctionId(), event.getSellerId(), type);

        userNotificationService.createUserNotification(response.getId(), event.getSellerId());
    }

    /**
     * CreatedBidEvent 발생 시
     * 입찰자 + 판매자 중
     * 알림 수신에 동의한 사람에게만 알림 발송
     *
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendNotificationOnCreatedBid(CreatedBidEvent event) {

        NotificationDto dto = userNotificationService.createUserNotification(event.getAuctionId(), event.getType(), event.getBidderId());

        List<GetNotificationTargetResponse> targets = userNotificationService.getNotificationTargets(dto.getNotificationId());

        targets.forEach(target ->
                notificationService.sendMessage(target.getUserId(), event.getType().getMessage())
        );
    }
}