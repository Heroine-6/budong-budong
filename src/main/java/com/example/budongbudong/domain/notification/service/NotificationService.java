package com.example.budongbudong.domain.notification.service;


import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.Notification;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.notification.client.KakaoClient;
import com.example.budongbudong.domain.notification.dto.CreateNotificationResponse;
import com.example.budongbudong.domain.notification.dto.KakaoNotificationResponse;
import com.example.budongbudong.domain.notification.dto.NotificationDto;
import com.example.budongbudong.domain.notification.enums.NotificationType;
import com.example.budongbudong.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final KakaoClient kakaoClient;

    // TODO: OAuth 로그인 구현 후 토큰 주입 방식 변경 예정
    @Value("${KAKAO_ACCESS_TOKEN}")
    private String accessToken;

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
                type.format(auction.getProperty().getName()),
                type,
                sellerId,
                auction
        );

        notificationRepository.save(newNotification);

        return CreateNotificationResponse.from(newNotification);
    }

    private String createTextMessage(String content) {

        // TODO: 우리 서비스로 연결 가능한 url 변경 예정
        String webUrl = "https://developers.kakao.com";

        return """
                {
                    "object_type": "text",
                    "text": "%s",
                    "link": {"web_url": "%s"}
                }
                """.formatted(content, webUrl);
    }

    /**
     * 카카오톡 나에게 보내기
     * 전송 성공 result_code: 0
     */
    public void sendMessage(Long userId, String content) {

        try {
            KakaoNotificationResponse response = kakaoClient.sendToMeMessage("Bearer " + accessToken, createTextMessage(content));
            log.info("[알림] to: {}, 카카오톡 나에게 보내기 성공: {}, result_code: {}", userId, content, response.getResultCode());

        } catch (Exception e) {
            log.error("[알림] to: {}, 카카오톡 나에게 보내기 실패: {}", userId, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public NotificationDto getNotification(Long auctionId, NotificationType type) {

        Notification notification = notificationRepository.getByAuctionIdAndTypeEqualsOrThrow(auctionId, type);

        return NotificationDto.from(notification);
    }
}