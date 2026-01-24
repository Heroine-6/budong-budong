package com.example.budongbudong.domain.bid.service;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.Bid;
import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.common.response.CustomPageResponse;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.bid.dto.request.CreateBidRequest;
import com.example.budongbudong.domain.bid.dto.response.CreateBidResponse;
import com.example.budongbudong.domain.bid.dto.response.ReadAllBidsResponse;
import com.example.budongbudong.domain.bid.dto.response.ReadMyBidsResponse;
import com.example.budongbudong.domain.bid.enums.BidStatus;
import com.example.budongbudong.domain.bid.repository.BidRepository;
import com.example.budongbudong.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;

    /**
     * 입찰 등록
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CreateBidResponse createBid(CreateBidRequest request, Long auctionId, Long userId) {

        String th = Thread.currentThread().getName();
        long t0 = System.currentTimeMillis();

        User user = userRepository.getByIdOrThrow(userId);

        log.info("[{}] t={} TRY_LOCK auctionId={}", th, System.currentTimeMillis(), auctionId);

        Auction auction = auctionRepository.getOpenAuctionForUpdateOrThrow(auctionId);

        log.info("[{}] t={} LOCK_ACQUIRED auctionId={} waited={}ms", th, System.currentTimeMillis(), auctionId, (System.currentTimeMillis() - t0));

        Long bidPrice = request.getPrice();
        Long currentMaxPrice = bidRepository.findMaxPriceByAuctionId(auctionId);

        if (currentMaxPrice != null && bidPrice <= currentMaxPrice) {
            log.info("[{}] FAIL_TOO_LOW auctionId={} bid={} max={}", th, auctionId, bidPrice, currentMaxPrice);
            return CreateBidResponse.rejectedFrom(BidStatus.REJECTED, "입찰가는 현재 최고가보다 높아야 합니다.");
        }

        bidRepository.unmarkHighestAndOutbidByAuctionId(auctionId);

        Bid bid = new Bid(user, auction, bidPrice);
        bid.markHighest();
        bid.changeStatus(BidStatus.WINNING);

        Bid savedBid = bidRepository.save(bid);

        log.info("[{}] SUCCESS auctionId={} price={}", th, auctionId, bidPrice);

        return CreateBidResponse.from(savedBid);
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
