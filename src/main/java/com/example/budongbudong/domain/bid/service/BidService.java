package com.example.budongbudong.domain.bid.service;

import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.domain.auction.entity.Auction;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.bid.dto.request.CreateBidRequest;
import com.example.budongbudong.domain.bid.dto.response.CreateBidResponse;
import com.example.budongbudong.domain.bid.dto.response.ReadAllBidsResponse;
import com.example.budongbudong.domain.bid.dto.response.ReadMyBidsResponse;
import com.example.budongbudong.domain.bid.entity.Bid;
import com.example.budongbudong.domain.bid.enums.BidStatus;
import com.example.budongbudong.domain.bid.repository.BidRepository;
import com.example.budongbudong.domain.user.entity.User;
import com.example.budongbudong.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;

    /**
     * 입찰 등록
     */
    @Transactional
    public CreateBidResponse createBid(CreateBidRequest request, Long auctionId, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));

        if (auction.getStatus() != AuctionStatus.OPEN) {
            throw new CustomException(ErrorCode.AUCTION_NOT_OPEN);
        }

        Long bidPrice = request.getPrice();
        Bid highestBid = bidRepository.findHighestBidByAuctionId(auctionId).orElse(null);

        if (highestBid != null && bidPrice <= highestBid.getPrice()) {
            throw new CustomException(ErrorCode.BID_PRICE_TOO_LOW);
        }

        if (highestBid != null) {
            highestBid.unmarkHighest();
            highestBid.changeStatus(BidStatus.OUTBID);
        }

        Bid bid = new Bid(user, auction, bidPrice);

        Bid savedBid = bidRepository.save(bid);

        return CreateBidResponse.from(savedBid);
    }

    /**
     * 입찰 내역 조회
     */
    @Transactional(readOnly = true)
    public Page<ReadAllBidsResponse> readAllBids(Long auctionId, Pageable pageable) {

        auctionRepository.findById(auctionId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));

        return bidRepository.findAllByAuctionId(auctionId, pageable)
                .map(ReadAllBidsResponse::from);

    }

    /**
     * 내 입찰 내역 조회
     */
    @Transactional(readOnly = true)
    public Page<ReadMyBidsResponse> readMyBids(Long userId, Pageable pageable) {

        return bidRepository.findMyBidsPage(userId, pageable)
                .map(ReadMyBidsResponse::from);
    }
}
