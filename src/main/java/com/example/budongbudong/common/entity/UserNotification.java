package com.example.budongbudong.common.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "user_notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserNotification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public static UserNotification create(Notification notification, User user) {
        UserNotification userNotification = new UserNotification();
        userNotification.notification = notification;
        userNotification.user = user;
        return userNotification;
    }
}
