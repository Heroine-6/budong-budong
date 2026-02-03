package com.example.budongbudong.domain.notification.service;


import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.Notification;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.notification.dto.CreateNotificationResponse;
import com.example.budongbudong.domain.notification.enums.NotificationType;
import com.example.budongbudong.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final AuctionRepository auctionRepository;
    private final NotificationRepository notificationRepository;

    /**
     * 알림 생성
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CreateNotificationResponse createSellerNotification(Long auctionId, Long sellerId, NotificationType type) {

        Auction auction = auctionRepository.getByIdOrThrow(auctionId);

        Optional<Notification> notification = notificationRepository.findByAuctionIdAndTypeEquals(auctionId, type);

        if (notification.isPresent()) {
            return CreateNotificationResponse.from(notification.get());
        }

        Notification newNotification = Notification.create(
                type.getMessage(),
                type,
                sellerId,
                auction
        );

        notificationRepository.save(newNotification);

        return CreateNotificationResponse.from(newNotification);
    }
}