package com.example.budongbudong.domain.notification.service;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.Notification;
import com.example.budongbudong.common.entity.Payment;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.notification.client.KakaoClient;
import com.example.budongbudong.domain.notification.dto.NotificationDto;
import com.example.budongbudong.domain.notification.dto.response.CreateNotificationResponse;
import com.example.budongbudong.domain.notification.dto.response.KakaoNotificationResponse;
import com.example.budongbudong.domain.notification.enums.NotificationType;
import com.example.budongbudong.domain.notification.repository.NotificationRepository;
import com.example.budongbudong.domain.payment.enums.PaymentType;
import com.example.budongbudong.domain.payment.repository.PaymentRepository;
import com.example.budongbudong.domain.payment.utils.PaymentAmountCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AuctionRepository auctionRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentAmountCalculator calculator;

    private final KakaoClient kakaoClient;

    // TODO: OAuth 로그인 구현 후 토큰 주입 방식 변경 예정
    @Value("${KAKAO_ACCESS_TOKEN}")
    private String accessToken;

    private String getFormatDateTime(LocalDateTime dateTime) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm");

        return dateTime.format(formatter);
    }

    private String getFormatDecimal(BigDecimal amount) {

        // 결제 금액 3자리마다 콤마로 구분
        DecimalFormat decimalFormat = new DecimalFormat("#,###");

        return decimalFormat.format(amount);
    }

    /**
     * 알림 생성
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CreateNotificationResponse createSellerNotification(
            Long auctionId,
            Long sellerId,
            NotificationType type
    ) {

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

    /**
     * 결제 요청 알림 생성
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CreateNotificationResponse createPaymentRequestNotification(
            Long auctionId,
            PaymentType paymentType,
            NotificationType notificationType,
            LocalDate baseDate
    ) {
        Auction auction = auctionRepository.getByIdOrThrow(auctionId);
        Long sellerId = auction.getProperty().getUser().getId();

        BigDecimal amount = calculator.calculate(auction, paymentType);
        String formattedAmount = getFormatDecimal(amount);

        LocalDate dueDate = baseDate.plusDays(paymentType.getDueDays());
        LocalDateTime dueDateTime = dueDate.atTime(23, 59);

        String content = notificationType.format(
                auction.getProperty().getName(),
                paymentType.getMessage(),
                formattedAmount,
                getFormatDateTime(dueDateTime)
        );

        Notification notification = Notification.create(
                content,
                notificationType,
                sellerId,
                auction
        );

        notificationRepository.save(notification);

        return CreateNotificationResponse.from(notification);
    }

    /**
     * 결제 완료 알림 생성
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CreateNotificationResponse createPaymentCompletedNotification(NotificationType notificationType, Long paymentId) {

        Payment payment = paymentRepository.getByIdOrThrow(paymentId);

        // 결제 승인 일시 문자열 변환
        LocalDateTime approvedAt = payment.getApprovedAt();
        String approvedDateTime = getFormatDateTime(approvedAt);

        Auction auction = auctionRepository.getByIdOrThrow(payment.getAuction().getId());
        Long sellerId = auction.getProperty().getUser().getId();

        String formattedAmount = getFormatDecimal(payment.getAmount());

        String content = notificationType.format(
                payment.getOrderName(),
                payment.getType().getMessage(),
                payment.getOrderId(),
                formattedAmount,
                approvedDateTime
        );

        Notification notification = Notification.create(
                content,
                notificationType,
                sellerId,
                auction
        );

        notificationRepository.save(notification);

        return CreateNotificationResponse.from(notification);
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

    @Transactional(readOnly = true)
    public NotificationDto getNotification(Long auctionId, NotificationType type) {

        Notification notification = notificationRepository.getByAuctionIdAndTypeEqualsOrThrow(auctionId, type);

        return NotificationDto.from(notification);
    }
}