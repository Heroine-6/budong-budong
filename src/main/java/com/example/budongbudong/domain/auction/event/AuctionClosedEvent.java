package com.example.budongbudong.domain.auction.event;

/**
 * 경매가 종료되었음을 나타내는 도메인 이벤트 객체
 * - CLOSED상태
 * - 낙찰처리 가능
 */
public record AuctionClosedEvent(Long auctionId) {
}
