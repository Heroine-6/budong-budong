package com.example.budongbudong.domain.auction.service;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.Bid;
import com.example.budongbudong.common.entity.Property;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.domain.auction.dto.request.CreateAuctionRequest;
import com.example.budongbudong.domain.auction.dto.request.CreateDutchAuctionRequest;
import com.example.budongbudong.domain.auction.dto.response.*;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.bid.repository.BidRepository;
import com.example.budongbudong.domain.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
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
        BigDecimal startPrice = request.getStartPrice();
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

        Auction auction = Auction.createEnglish(
                property,
                startPrice,
                startedAt,
                endedAt
        );

        auctionRepository.save(auction);

        return CreateAuctionResponse.from(auction);
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
        BigDecimal highestPrice = bidRepository.getHighestPriceOrStartPrice(auctionId, auction.getStartPrice());

        // 총 입찰자 수
        int totalBidders = bidRepository.countDistinctBiddersByAuctionId(auctionId);

        return AuctionInfoResponse.from(auction, highestPrice, totalBidders);
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
        BigDecimal priceIncrease = new BigDecimal(0);

        if (totalBidCount == 1L) {
            priceIncrease = Objects.requireNonNull(highestBid).getPrice().subtract(auction.getStartPrice());
        } else if (totalBidCount > 1L) {
            priceIncrease = Objects.requireNonNull(highestBid).getPrice().subtract(bidList.get(1).getPrice());
        }

        return GetStatisticsResponse.from(
                auctionId,
                totalBidders,
                totalBidCount,
                priceIncrease,
                createdAt
        );
    }


    /**
     * 네덜란드식 경매 등록
     * - 본인 소유의 매물만 경매 등록 가능
     * - 시작일은 익일부터 가능하며, 경매 기간은 하루(시작 당일 종료)
     */
    @Transactional
    public CreateDutchAuctionResponse createDutchAuction(CreateDutchAuctionRequest request, Long userId) {

        Long propertyId = request.getPropertyId();
        LocalDateTime startedAt = request.getStartedAt();
        BigDecimal startPrice = request.getStartPrice();
        BigDecimal endPrice = request.getEndPrice();
        int decreaseRate = request.getDecreaseRate();

        Property property = propertyRepository.getByIdAndNotDeletedOrThrow(propertyId);

        if (!userId.equals(property.getUser().getId())) {
            throw new CustomException(ErrorCode.USER_NOT_MATCH);
        }

        auctionRepository.getByPropertyIdOrThrowIfExists(property.getId());

        LocalDate today = LocalDate.now();
        LocalDateTime possibleStartDate = today.atStartOfDay().plusDays(1);

        if (startedAt.isBefore(possibleStartDate)) {
            throw new CustomException(ErrorCode.INVALID_AUCTION_PERIOD);
        }

        Auction auction = Auction.createDutch(
                property,
                startPrice,
                endPrice,
                decreaseRate,
                startedAt
        );

        auctionRepository.save(auction);

        return CreateDutchAuctionResponse.from(auction);
    }
}
