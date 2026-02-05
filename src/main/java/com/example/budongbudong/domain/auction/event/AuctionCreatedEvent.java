package com.example.budongbudong.domain.auction.event;

/**
 * 경매가 생성되었음을 나타내는 도메인 이벤트 객체
 * - 알림 발송
 */
public record AuctionCreatedEvent(Long auctionId, Long sellerId) {
}