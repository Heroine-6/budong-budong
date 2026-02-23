package com.example.budongbudong.domain.notification.usernotification.service;

import com.example.budongbudong.common.entity.Notification;
import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.common.entity.UserNotification;
import com.example.budongbudong.common.response.CustomSliceResponse;
import com.example.budongbudong.domain.bid.repository.BidRepository;
import com.example.budongbudong.domain.notification.repository.NotificationRepository;
import com.example.budongbudong.domain.notification.usernotification.dto.NotificationTargetResponse;
import com.example.budongbudong.domain.notification.usernotification.dto.UserNotificationResponse;
import com.example.budongbudong.domain.notification.usernotification.repository.UserNotificationRepository;
import com.example.budongbudong.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
    private final BidRepository bidRepository;

    /**
     * 알림 수신자 등록
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationTargetResponse createUserNotification(Long notificationId, Long userId) {

        Notification notification = notificationRepository.getByIdOrThrow(notificationId);
        User user = userRepository.getByIdOrThrow(userId);

        UserNotification userNotification = UserNotification.create(notification, user);
        userNotificationRepository.save(userNotification);

        return NotificationTargetResponse.from(userNotification);
    }

    /**
     * 알림 수신자 등록
     * 판매자 + 모든 입찰자
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<NotificationTargetResponse> createUserNotificationAllUsers(Long notificationId, Long auctionId) {

        Notification notification = notificationRepository.getByIdOrThrow(notificationId);

        User seller = userRepository.getByIdOrThrow(notification.getSellerId());
        List<User> targets = bidRepository.findAllBiddersByAuctionId(auctionId);

        targets.add(seller);

        return targets.stream()
                .map(target -> {
                    UserNotification userNotification = UserNotification.create(notification, target);
                    userNotificationRepository.save(userNotification);
                    return NotificationTargetResponse.from(userNotification);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomSliceResponse<UserNotificationResponse> getAllUserNotificationList(Long userId, Pageable pageable) {

        Slice<UserNotification> userNotifications = userNotificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        Slice<UserNotificationResponse> response = userNotifications.map(UserNotificationResponse::from);

        return CustomSliceResponse.from(response.getContent(), pageable.getPageSize(), pageable.getPageNumber(), response.hasNext());
    }

}