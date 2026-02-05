package com.example.budongbudong.domain.auction.event;

/**
 * 경매가 곧 종료됨을 나타내는 도메인 이벤트 객체
 * - 알림 발송
 */
public record AuctionEndingSoonEvent(Long auctionId) {
}
