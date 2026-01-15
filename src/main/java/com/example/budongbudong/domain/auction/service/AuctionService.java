package com.example.budongbudong.domain.auction.service;

import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.domain.auction.dto.request.CreateAuctionRequest;
import com.example.budongbudong.domain.auction.dto.response.CreateAuctionResponse;
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

    /**
     * 매물 등록
     */
    @Transactional
    public CreateAuctionResponse createAuction(CreateAuctionRequest request) {

        Long propertyId = request.getPropertyId();
        Long startPrice = request.getStartPrice();
        LocalDateTime startedAt = request.getStartedAt();
        LocalDateTime endedAt = request.getEndedAt();

        Property property = propertyRepository.findById(propertyId).orElseThrow(
                () -> new CustomException(ErrorCode.PROPERTY_NOT_FOUND));

        Auction auction = auctionRepository.findByPropertyId(propertyId).orElse(null);

        if(auction != null && !auction.getStatus().equals(AuctionStatus.CANCELLED)) {
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
}
