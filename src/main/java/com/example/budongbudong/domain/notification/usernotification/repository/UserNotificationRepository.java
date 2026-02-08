package com.example.budongbudong.domain.notification.usernotification.repository;

import com.example.budongbudong.common.entity.UserNotification;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    default UserNotification getByIdOrThrow(Long userNotificationId) {
        return findById(userNotificationId).orElseThrow(() -> new CustomException(ErrorCode.USER_NOTIFICATION_NOT_FOUND));
    }
}