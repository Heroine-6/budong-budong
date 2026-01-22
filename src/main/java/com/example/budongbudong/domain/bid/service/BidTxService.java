package com.example.budongbudong.domain.bid.service;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.Bid;
import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.bid.dto.request.CreateBidRequest;
import com.example.budongbudong.domain.bid.dto.response.CreateBidResponse;
import com.example.budongbudong.domain.bid.enums.BidStatus;
import com.example.budongbudong.domain.bid.repository.BidRepository;
import com.example.budongbudong.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidTxService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;

    /**
     * 입찰 등록 - Create
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CreateBidResponse createBidTx(CreateBidRequest request, Long auctionId, Long userId) {

        String th = Thread.currentThread().getName();

        User user = userRepository.getByIdOrThrow(userId);

        Auction auction = auctionRepository.getOpenAuctionOrThrow(auctionId);

        Long bidPrice = request.getPrice();
        Long currentMaxPrice = bidRepository.findMaxPriceByAuctionId(auctionId);

        Long minBidIncrement = auction.getMinBidIncrement();

        // 첫 입찰은 시작가 이상, 이후 입찰은 최고가 + 최소 입찰 단위 이상.
        Long minimumRequired = currentMaxPrice == null
                ? auction.getStartPrice()
                : currentMaxPrice + minBidIncrement;

        if (bidPrice < minimumRequired) {
            log.info("[{}] 최소입찰미달 auctionId={} bid={} minimum={}", th, auctionId, bidPrice, minimumRequired);
            throw new CustomException(ErrorCode.BID_PRICE_TOO_LOW);
        }

        if ((bidPrice - minimumRequired) % minBidIncrement != 0) {
            log.info("[{}] 입찰단위오류 auctionId={} bid={} minimum={} increment={}", th, auctionId, bidPrice, minimumRequired, minBidIncrement);
            throw new CustomException(ErrorCode.INVALID_BID_PRICE);
        }

        bidRepository.unmarkHighestAndOutbidByAuctionId(auctionId);

        Bid bid = new Bid(user, auction, bidPrice);
        bid.markHighest();
        bid.changeStatus(BidStatus.WINNING);

        Bid savedBid = bidRepository.save(bid);

        log.info("[{}] SUCCESS auctionId={} price={}", th, auctionId, bidPrice);

        return CreateBidResponse.from(savedBid);
    }
}
