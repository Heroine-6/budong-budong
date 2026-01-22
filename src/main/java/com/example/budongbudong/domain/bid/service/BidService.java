package com.example.budongbudong.domain.bid.service;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.Bid;
import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
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

        // 최소 입찰 단위가 누락된 데이터는 시작가 10%를 올림한 값으로 보정
        Long minBidIncrement = auction.getMinBidIncrement();
        if (minBidIncrement == null) {
            minBidIncrement = (auction.getStartPrice() + 9) / 10;
        }

        // 첫 입찰은 시작가 이상, 이후 입찰은 최고가 + 최소 입찰 단위 이상
        Long minimumRequired = currentMaxPrice == null
                ? auction.getStartPrice()
                : currentMaxPrice + minBidIncrement;

        if (bidPrice < minimumRequired) {
            log.info("[{}] FAIL_TOO_LOW auctionId={} bid={} minimum={}", th, auctionId, bidPrice, minimumRequired);
            throw new CustomException(ErrorCode.BID_PRICE_TOO_LOW);
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
