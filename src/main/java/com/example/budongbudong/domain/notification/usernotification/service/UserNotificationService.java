package com.example.budongbudong.domain.notification.usernotification.service;

import com.example.budongbudong.common.entity.Notification;
import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.common.entity.UserNotification;
import com.example.budongbudong.domain.notification.dto.NotificationDto;
import com.example.budongbudong.domain.notification.enums.NotificationType;
import com.example.budongbudong.domain.notification.repository.NotificationRepository;
import com.example.budongbudong.domain.notification.usernotification.dto.GetNotificationTargetResponse;
import com.example.budongbudong.domain.notification.usernotification.repository.UserNotificationRepository;
import com.example.budongbudong.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserNotificationService {

    private final UserNotificationRepository userNotificationRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * 알림 수신자 등록
     *
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createUserNotification(Long notificationId, Long userId) {

        Notification notification = notificationRepository.getByIdOrThrow(notificationId);

        saveUserNotification(userId, notification);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationDto createUserNotification(Long auctionId, NotificationType type, Long userId) {

        Notification notification = notificationRepository.getByAuctionIdAndTypeEqualsOrThrow(auctionId, type);

        saveUserNotification(userId, notification);

        return NotificationDto.from(notification);
    }

    private void saveUserNotification(Long userId, Notification notification) {

        User user = userRepository.getByIdOrThrow(userId);

        boolean existUserNotification = userNotificationRepository.existsByNotificationIdAndUserId(notification.getId(), userId);

        if (existUserNotification) {
            return;
        }

        UserNotification userNotification = UserNotification.create(notification, user);

        userNotificationRepository.save(userNotification);
    }

    // 입찰 시 알림 수신자 검색
    @Transactional(readOnly = true)
    public List<GetNotificationTargetResponse> getNotificationTargets(Long notificationId) {

        List<UserNotification> targets = userNotificationRepository.findPushTargets(notificationId);

        return targets.stream().map(GetNotificationTargetResponse::from).toList();
    }
}