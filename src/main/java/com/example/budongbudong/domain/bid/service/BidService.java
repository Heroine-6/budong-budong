package com.example.budongbudong.domain.bid.service;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.Bid;
import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.common.response.CustomPageResponse;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.auction.event.AuctionClosedEvent;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.bid.dto.request.CreateBidRequest;
import com.example.budongbudong.domain.bid.dto.response.CreateBidResponse;
import com.example.budongbudong.domain.bid.dto.response.ReadAllBidsResponse;
import com.example.budongbudong.domain.bid.dto.response.ReadMyBidsResponse;
import com.example.budongbudong.domain.bid.enums.BidStatus;
import com.example.budongbudong.domain.bid.event.BidCreatedEvent;
import com.example.budongbudong.domain.bid.repository.BidRepository;
import com.example.budongbudong.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final RedissonClient redissonClient;
    private final BidTxService bidTxService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 입찰 등록 - Lock
     */
    public CreateBidResponse createBid(CreateBidRequest request, Long auctionId, Long userId) {

        long t0 = System.currentTimeMillis();
        String th = Thread.currentThread().getName();

        LocalDateTime endedAt = auctionRepository.getEndedAtOrThrow(auctionId);
        LocalDateTime now = LocalDateTime.now();

        boolean lastHour = !now.isBefore(endedAt.minusHours(1));

        long waitTime = lastHour ? 2L : 0L;

        String lockKey = "lock:auction:" + auctionId;
        RLock lock = redissonClient.getFairLock(lockKey);

        boolean acquired = false;

        try {
            log.info("[{}] t={} TRY_LOCK auctionId={}", th, System.currentTimeMillis(), auctionId);

            acquired = lock.tryLock(waitTime, -1, TimeUnit.SECONDS);

            if (!acquired) {
                log.info("[{}] LOCK_FAILED auctionId={} waited={}ms", th, auctionId, System.currentTimeMillis() - t0);
                throw new CustomException(ErrorCode.BID_LOCK_TIMEOUT);
            }

            log.info("[{}] LOCK_ACQUIRED auctionId={} waited={}ms", th, auctionId, System.currentTimeMillis() - t0);

            return bidTxService.createBidTx(request, auctionId, userId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.BID_LOCK_FAILED);

        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 입찰 내역 조회
     */
    @Transactional(readOnly = true)
    public Page<ReadAllBidsResponse> readAllBids(Long auctionId, Pageable pageable) {

        auctionRepository.validateExistsOrThrow(auctionId);

        return bidRepository.findAllByAuctionId(auctionId, pageable)
                .map(ReadAllBidsResponse::from);

    }

    /**
     * 내 입찰 내역 조회
     */
    @Transactional(readOnly = true)
    public CustomPageResponse<ReadMyBidsResponse> readMyBids(Long userId, String status, Pageable pageable) {

        Page<ReadMyBidsResponse> page = bidRepository.findMyBids(userId, status, pageable);
        return CustomPageResponse.from(page);
    }

    /**
     * 네덜란드식 경매 입찰 등록
     * - 입찰가(현재가) 계산
     * - 입찰 -> 경매 종료(이벤트 발행) -> (이벤트리스너)낙찰 처리
     */
    @Transactional
    public CreateBidResponse createDutchBid(Long auctionId, Long userId) {

        User user = userRepository.getByIdOrThrow(userId);

        Auction auction = auctionRepository.getOpenAuctionOrThrow(auctionId);

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
}
