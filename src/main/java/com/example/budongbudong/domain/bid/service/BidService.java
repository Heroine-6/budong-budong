package com.example.budongbudong.domain.bid.service;

import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.common.response.CustomPageResponse;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.bid.dto.request.CreateBidRequest;
import com.example.budongbudong.domain.bid.dto.response.CreateBidResponse;
import com.example.budongbudong.domain.bid.dto.response.ReadAllBidsResponse;
import com.example.budongbudong.domain.bid.dto.response.ReadMyBidsResponse;
import com.example.budongbudong.domain.bid.repository.BidRepository;
import com.example.budongbudong.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 입찰 등록 - Lock
     */
    public CreateBidResponse createBid(CreateBidRequest request, Long auctionId, Long userId) {

        long t0 = System.currentTimeMillis();
        String th = Thread.currentThread().getName();

        String lockKey = "lock:auction:" + auctionId;
        RLock lock = redissonClient.getLock(lockKey);

        boolean acquired = false;

        try {
            acquired = lock.tryLock(0, 5, TimeUnit.SECONDS);

            log.info("[{}] t={} TRY_LOCK auctionId={}", th, System.currentTimeMillis(), auctionId);

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
}
