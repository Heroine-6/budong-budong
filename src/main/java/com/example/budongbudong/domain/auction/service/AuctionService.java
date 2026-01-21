package com.example.budongbudong.domain.auction.service;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.Bid;
import com.example.budongbudong.common.entity.Property;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.domain.auction.dto.request.CreateAuctionRequest;
import com.example.budongbudong.domain.auction.dto.response.AuctionInfoResponse;
import com.example.budongbudong.domain.auction.dto.response.CancelAuctionResponse;
import com.example.budongbudong.domain.auction.dto.response.CreateAuctionResponse;
import com.example.budongbudong.domain.auction.dto.response.GetStatisticsResponse;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.bid.repository.BidRepository;
import com.example.budongbudong.domain.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
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

        Property property = propertyRepository.getByIdAndNotDeletedOrThrow(propertyId);

        if (!userId.equals(property.getUser().getId())) {
            throw new CustomException(ErrorCode.USER_NOT_MATCH);
        }

        auctionRepository.getByPropertyIdOrThrowIfExists(property.getId());

        if (startedAt.isBefore(LocalDateTime.now()) || startedAt.isAfter(endedAt)) {
            throw new CustomException(ErrorCode.INVALID_AUCTION_PERIOD);
        }

        LocalDateTime maxEndedAt = startedAt.plusDays(7);

        if (endedAt.isAfter(maxEndedAt)) {
            throw new CustomException(ErrorCode.MAX_AUCTION_PERIOD_EXCEEDED);
        }

        Auction auction = Auction.create(
                property,
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

        Auction auction = auctionRepository.getByIdOrThrow(auctionId);

        if (!userId.equals(auction.getProperty().getUser().getId())) {
            throw new CustomException(ErrorCode.USER_NOT_MATCH);
        }

        if (!auction.getStatus().equals(AuctionStatus.SCHEDULED)) {
            throw new CustomException(ErrorCode.AUCTION_INVALID_STATUS_FOR_CANCEL);
        }

        auction.updateStatus(AuctionStatus.CANCELLED);

        return CancelAuctionResponse.from(auction);
    }

    /**
     * 입찰 정보 조회
     */
    @Transactional(readOnly = true)
    public AuctionInfoResponse getAuctionInfo(Long auctionId) {

        Auction auction = auctionRepository.getByIdOrThrow(auctionId);

        // 최고 입찰가
        Long highestPrice = bidRepository.getHighestPriceOrStartPrice(auctionId, auction.getStartPrice());

        // 총 입찰자 수
        int totalBidders = bidRepository.countDistinctBiddersByAuctionId(auctionId);

        return new AuctionInfoResponse(
                auction.getStartPrice(),
                highestPrice,
                totalBidders,
                auction.getEndedAt()
        );
    }

    /**
     * 경쟁 정보 및 통계 조회
     */
    @Transactional(readOnly = true)
    public GetStatisticsResponse getAuctionStatistics(Long auctionId) {

        Auction auction = auctionRepository.getByIdOrThrow(auctionId);

        // 총 입찰자 수
        int totalBidders = bidRepository.countDistinctBiddersByAuctionId(auctionId);

        List<Bid> bidList = bidRepository.findAllByAuctionOrderByPriceDesc(auction);

        Bid highestBid = (bidList.isEmpty()) ? null : bidList.get(0);

        // 최근 입찰 시간
        LocalDateTime createdAt = (highestBid == null) ? null : highestBid.getCreatedAt();

        // 총 입찰 횟수
        int totalBidCount = bidList.size();

        // 입찰 가격 상승 금액
        long priceIncrease = 0L;

        if (totalBidCount == 1L) {
            priceIncrease = Objects.requireNonNull(highestBid).getPrice() - auction.getStartPrice();
        } else if (totalBidCount > 1L) {
            priceIncrease = Objects.requireNonNull(highestBid).getPrice() - bidList.get(1).getPrice();
        }

        return GetStatisticsResponse.from(
                auctionId,
                totalBidders,
                totalBidCount,
                priceIncrease,
                createdAt
        );
    }
}
