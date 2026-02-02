package com.example.budongbudong.domain.notification.usernotification;

import com.example.budongbudong.common.entity.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    boolean existsByNotificationIdAndUserId(Long notificationId, Long userId);
}