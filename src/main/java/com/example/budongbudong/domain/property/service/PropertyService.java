package com.example.budongbudong.domain.property.service;

import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.common.response.CustomPageResponse;
import com.example.budongbudong.domain.property.dto.request.UpdatePropertyRequest;
import com.example.budongbudong.domain.auction.dto.AuctionResponse;
import com.example.budongbudong.domain.auction.entity.Auction;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.property.dto.response.ReadAllPropertyResponse;
import com.example.budongbudong.domain.property.dto.response.ReadPropertyResponse;
import com.example.budongbudong.domain.property.entity.Property;
import com.example.budongbudong.domain.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PropertyService {
    private final PropertyRepository propertyRepository;
    private final AuctionRepository auctionRepository;

    @Transactional(readOnly = true)
    public CustomPageResponse<ReadAllPropertyResponse> getAllPropertyList(Pageable pageable) {

        Page<Property> propertyPage = propertyRepository.findAll(pageable);
        Page<ReadAllPropertyResponse> response = propertyPage.map(property -> {

            Auction auction = auctionRepository.findByPropertyId(property.getId()).orElse(null);
            AuctionResponse auctionResponse = (auction != null) ? AuctionResponse.from(auction) : null;

            return ReadAllPropertyResponse.from(property, auctionResponse);
        });

        return CustomPageResponse.from(response);
    }

    @Transactional(readOnly = true)
    public CustomPageResponse<ReadAllPropertyResponse> getMyPropertyList(Long userId, Pageable pageable) {

        Page<Property> propertyPage = propertyRepository.findAllByUserId(userId, pageable);
        Page<ReadAllPropertyResponse> response = propertyPage.map(property -> {

            Auction auction = auctionRepository.findByPropertyId(property.getId()).orElse(null);
            AuctionResponse auctionResponse = (auction != null) ? AuctionResponse.from(auction) : null;

            return ReadAllPropertyResponse.from(property, auctionResponse);
        });

        return CustomPageResponse.from(response);
    }

    @Transactional(readOnly = true)
    public ReadPropertyResponse getProperty(Long propertyId) {

        Property property = propertyRepository.findByIdWithImages(propertyId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROPERTY_NOT_FOUND));

        Auction auction = auctionRepository.findByPropertyId(propertyId).orElse(null);
        AuctionStatus auctionStatus = (auction != null) ? auction.getStatus() : null;

        return ReadPropertyResponse.from(property, auctionStatus);
    }

    @Transactional
    public void updateProperty(Long propertyId, UpdatePropertyRequest request) {

        Property property = propertyRepository.findByIdWithImages(propertyId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROPERTY_NOT_FOUND));

        property.update(
                request.getPrice(),
                request.getMigrateDate(),
                request.getDescription()
        );
    }

    @Transactional
    public void deleteProperty(Long propertyId) {

        Property property = propertyRepository.findByIdAndIsDeletedFalse(propertyId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROPERTY_NOT_FOUND));

        boolean hasNonScheduledAuction = auctionRepository.existsByPropertyIdAndStatusNot(propertyId, AuctionStatus.SCHEDULED);

        if (hasNonScheduledAuction) {
            throw new CustomException(ErrorCode.PROPERTY_CANNOT_DELETE);
        }

        property.softDelete();
    }
}
