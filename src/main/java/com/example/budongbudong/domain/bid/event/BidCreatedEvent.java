package com.example.budongbudong.domain.bid.event;

/**
 * 입찰이 생성되었음을 나타내는 도메인 이벤트 객체
 * - 알림 발송
 */
public record BidCreatedEvent(Long auctionId, Long bidderId) {
}
