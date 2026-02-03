package com.example.budongbudong.domain.notification.repository;

import com.example.budongbudong.common.entity.Notification;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.domain.notification.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByAuctionIdAndTypeEquals(Long auctionId, NotificationType type);

    default Notification getByIdOrThrow(Long notificationId) {
        return findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));
    }

    default Notification getByAuctionIdAndTypeEqualsOrThrow(Long auctionId, NotificationType type) {
        return findByAuctionIdAndTypeEquals(auctionId, type)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));
    }
}
