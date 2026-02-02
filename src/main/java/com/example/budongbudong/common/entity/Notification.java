package com.example.budongbudong.common.entity;

import com.example.budongbudong.domain.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "winner_id")
    private Long winnerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    public static Notification create(
            String content,
            NotificationType type,
            Long sellerId,
            Auction auction
    ) {
        Notification notification = new Notification();
        notification.content = content;
        notification.type = type;
        notification.sellerId = sellerId;
        notification.auction = auction;
        return notification;
    }

    public void updateWinnerId(Long winnerId) {
        this.winnerId = winnerId;
    }
}
