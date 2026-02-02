package com.example.budongbudong.domain.notification.usernotification;

import com.example.budongbudong.common.entity.Notification;
import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.common.entity.UserNotification;
import com.example.budongbudong.domain.notification.repository.NotificationRepository;
import com.example.budongbudong.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
        User user = userRepository.getByIdOrThrow(userId);

        boolean existUserNotification = userNotificationRepository.existsByNotificationIdAndUserId(notificationId, userId);

        if (existUserNotification) {
            return;
        }

        UserNotification userNotification = UserNotification.create(notification, user);

        userNotificationRepository.save(userNotification);
    }
}