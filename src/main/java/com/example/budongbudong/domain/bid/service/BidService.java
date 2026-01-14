package com.example.budongbudong.domain.bid.service;

import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.domain.auction.entity.Auction;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.bid.dto.CreateBidRequest;
import com.example.budongbudong.domain.bid.dto.CreateBidResponse;
import com.example.budongbudong.domain.bid.entity.Bid;
import com.example.budongbudong.domain.bid.enums.BidStatus;
import com.example.budongbudong.domain.bid.repository.BidRepository;
import com.example.budongbudong.domain.user.entity.User;
import com.example.budongbudong.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;

    /**
     * 입찰 등록
     */
    public CreateBidResponse createBid(CreateBidRequest request, Long auctionId) {

        // TODO: 임의로 auctionId 값을 userId로 사용 중
        User user = userRepository.findById(auctionId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));

        if (auction.getStatus() != AuctionStatus.OPEN) {
            throw new CustomException(ErrorCode.AUCTION_NOT_OPEN);
        }

        Long bidPrice = request.getPrice();

        if (bidPrice == null || bidPrice <= 0) {
            throw new CustomException(ErrorCode.INVALID_BID_PRICE);
        }

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
}
