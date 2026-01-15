package com.example.budongbudong.domain.auction.service;

import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.domain.auction.dto.request.CreateAuctionRequest;
import com.example.budongbudong.domain.auction.dto.response.AuctionInfoResponse;
import com.example.budongbudong.domain.auction.dto.response.CancelAuctionResponse;
import com.example.budongbudong.domain.auction.dto.response.CreateAuctionResponse;
import com.example.budongbudong.domain.bid.repository.BidRepository;
import com.example.budongbudong.domain.auction.entity.Auction;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.property.entity.Property;
import com.example.budongbudong.domain.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final PropertyRepository propertyRepository;
    private final BidRepository bidRepository;

    /**
     * 경매 등록
     */
    @Transactional
    public CreateAuctionResponse createAuction(CreateAuctionRequest request, Long userId) {

        Long propertyId = request.getPropertyId();
        Long startPrice = request.getStartPrice();
        LocalDateTime startedAt = request.getStartedAt();
        LocalDateTime endedAt = request.getEndedAt();

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROPERTY_NOT_FOUND));

        if(!userId.equals(property.getUser().getId())){
            throw new CustomException(ErrorCode.USER_NOT_MATCH);
        }

        Auction auction = auctionRepository.findByPropertyId(propertyId).orElse(null);

        if (auction != null && !auction.getStatus().equals(AuctionStatus.CANCELLED)) {
            throw new CustomException(ErrorCode.AUCTION_ALREADY_EXISTS);
        }

        if (startedAt.isBefore(LocalDateTime.now()) || startedAt.isAfter(endedAt)) {
            throw new CustomException(ErrorCode.INVALID_AUCTION_PERIOD);
        }

        auction = Auction.create(property,
                startPrice,
                startedAt,
                endedAt
        );

        auctionRepository.save(auction);

        Long bidPrice = startPrice / 10;
        return CreateAuctionResponse.from(auction, bidPrice);
    }

    /**
     * 경매 상태 변경 (취소)
     */
    @Transactional
    public CancelAuctionResponse cancelAuction(Long auctionId, Long userId) {

        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));

        if(!userId.equals(auction.getProperty().getUser().getId())){
            throw new CustomException(ErrorCode.USER_NOT_MATCH);
        }

        if (!auction.getStatus().equals(AuctionStatus.SCHEDULED)) {
            throw new CustomException(ErrorCode.AUCTION_INVALID_STATUS_FOR_CANCEL);
        }

        auction.updateStatus(AuctionStatus.CANCELLED);

        return CancelAuctionResponse.from(auction);
    }

    @Transactional(readOnly = true)
    public AuctionInfoResponse getAuctionInfo(Long auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));

        // 최고 입찰가
        Long highestPrice = bidRepository.findHighestPriceByAuctionId(auctionId)
                .orElse(auction.getStartPrice());

        // 총 입찰자 수
        Long totalBidders = bidRepository.countDistinctBiddersByAuctionId(auctionId);

        return new AuctionInfoResponse(
                auction.getStartPrice(),
                highestPrice,
                totalBidders,
                auction.getEndedAt()
        );
    }
}
