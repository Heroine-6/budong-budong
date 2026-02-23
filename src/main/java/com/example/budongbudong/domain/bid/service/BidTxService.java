package com.example.budongbudong.domain.bid.service;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.Bid;
import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.auction.event.AuctionClosedEvent;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.bid.dto.request.CreateBidRequest;
import com.example.budongbudong.domain.bid.dto.response.CreateBidResponse;
import com.example.budongbudong.domain.bid.enums.BidStatus;
import com.example.budongbudong.domain.bid.event.BidCreatedEvent;
import com.example.budongbudong.domain.bid.repository.BidRepository;
import com.example.budongbudong.domain.payment.enums.PaymentStatus;
import com.example.budongbudong.domain.payment.enums.PaymentType;
import com.example.budongbudong.domain.payment.repository.PaymentRepository;
import com.example.budongbudong.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidTxService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentRepository paymentRepository;

    /**
     * 입찰 등록 - Create
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CreateBidResponse createBidTx(CreateBidRequest request, Long auctionId, Long userId) {

        String th = Thread.currentThread().getName();

        User user = userRepository.getByIdOrThrow(userId);

        Auction auction = auctionRepository.getOpenAuctionOrThrow(auctionId);

        //가장 먼저 보증금 납부 여부 확인
        validateDeposit(user, auction);

        BigDecimal bidPrice = request.getPrice();
        BigDecimal currentMaxPrice = bidRepository.findMaxPriceByAuctionId(auctionId);

        BigDecimal minBidIncrement = auction.getMinBidIncrement();

        // 첫 입찰은 시작가 이상, 이후 입찰은 최고가 + 최소 입찰 단위 이상.
        BigDecimal minimumRequired = currentMaxPrice == null
                ? auction.getStartPrice()
                : currentMaxPrice.add(minBidIncrement);

        if (bidPrice.compareTo(minimumRequired) < 0) {
            log.info("[{}] 최소입찰미달 auctionId={} bid={} minimum={}", th, auctionId, bidPrice, minimumRequired);
            return CreateBidResponse.rejectedFrom(BidStatus.REJECTED, "입찰가는 현재 최고가보다 높아야 합니다.");
        }

        if ((bidPrice.subtract(minimumRequired)).remainder(minBidIncrement).compareTo(BigDecimal.ZERO) != 0) {
            log.info("[{}] 입찰단위오류 auctionId={} bid={} minimum={} increment={}", th, auctionId, bidPrice, minimumRequired, minBidIncrement);
            return CreateBidResponse.rejectedFrom(BidStatus.REJECTED, "입찰 금액이 올바르지 않습니다.");
        }

        bidRepository.unmarkHighestAndOutbidByAuctionId(auctionId);

        Bid bid = new Bid(user, auction, bidPrice);
        bid.markHighest();
        bid.changeStatus(BidStatus.WINNING);

        Bid savedBid = bidRepository.save(bid);

        log.info("[{}] SUCCESS auctionId={} price={}", th, auctionId, bidPrice);

        eventPublisher.publishEvent(new BidCreatedEvent(auctionId, userId));

        return CreateBidResponse.from(savedBid);
    }

    /**
     * 네덜란드식 경매 입찰 등록
     * - 입찰가(현재가) 계산
     * - 입찰 -> 경매 종료(이벤트 발행) -> (이벤트리스너)낙찰 처리
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CreateBidResponse createDutchBid(Long auctionId, Long userId) {

        User user = userRepository.getByIdOrThrow(userId);

        Auction auction = auctionRepository.getOpenAuctionOrThrow(auctionId);

        //가장 먼저 보증금 납부 여부 확인
        validateDeposit(user, auction);

        // 현재 시각 기준으로 가격 재계산
        long minutesElapsed = Duration.between(auction.getStartedAt(), LocalDateTime.now()).toMinutes();
        auction.recalculateCurrentPrice(minutesElapsed);

        if (auction.getStatus() != AuctionStatus.OPEN) {
            throw new CustomException(ErrorCode.AUCTION_NOT_OPEN);
        }

        BigDecimal bidPrice = auction.getCurrentPrice();

        Bid bid = new Bid(user, auction, bidPrice);
        bid.markHighest();
        bid.changeStatus(BidStatus.WINNING);

        Bid savedBid = bidRepository.save(bid);

        log.debug("[입찰] 성공 - auctionId={}, bidId={}", auctionId, bid.getId());

        eventPublisher.publishEvent(new BidCreatedEvent(auctionId, bid.getId()));

        // 경매 종료
        auctionRepository.closeIfOpen(auctionId);
        eventPublisher.publishEvent(new AuctionClosedEvent(auctionId));

        return CreateBidResponse.from(savedBid);
    }

    private void validateDeposit(User user, Auction auction) {
        boolean hasDeposit = paymentRepository.existsByUserAndAuctionAndTypeAndStatus(user, auction, PaymentType.DEPOSIT, PaymentStatus.SUCCESS);

        if (!hasDeposit) {
            throw new CustomException(ErrorCode.DEPOSIT_REQUIRED);
        }
    }

}
