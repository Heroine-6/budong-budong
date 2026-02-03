package com.example.budongbudong.domain.notification.usernotification.repository;

import com.example.budongbudong.common.entity.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    boolean existsByNotificationIdAndUserId(Long notificationId, Long userId);

    @Query("""
            select un
            from UserNotification un
            join fetch un.user u
            where un.notification.id = :notificationId
            and u.isPushAllowed = true
            """)
    List<UserNotification> findPushTargets(@Param("notificationId") Long notificationId);
}