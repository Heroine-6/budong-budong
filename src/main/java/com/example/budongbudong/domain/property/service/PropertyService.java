package com.example.budongbudong.domain.property.service;

import com.example.budongbudong.common.api.AptItem;
import com.example.budongbudong.common.api.AptMapper;
import com.example.budongbudong.common.api.AptResponse;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.common.response.CustomPageResponse;
import com.example.budongbudong.common.api.AptClient;
import com.example.budongbudong.domain.property.dto.request.CreatePropertyRequestDTO;
import com.example.budongbudong.domain.property.dto.request.UpdatePropertyRequest;
import com.example.budongbudong.domain.auction.dto.response.AuctionResponse;
import com.example.budongbudong.domain.auction.entity.Auction;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.property.dto.response.CreateApiResponse;
import com.example.budongbudong.domain.property.dto.response.ReadAllPropertyResponse;
import com.example.budongbudong.domain.property.dto.response.ReadPropertyResponse;
import com.example.budongbudong.domain.property.entity.Property;
import com.example.budongbudong.domain.property.repository.PropertyRepository;
import com.example.budongbudong.domain.propertyimage.service.PropertyImageService;
import com.example.budongbudong.domain.user.entity.User;
import com.example.budongbudong.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final PropertyImageService propertyImageService;
    private final AuctionRepository auctionRepository;
    private final AptClient aptClient;

    @Value("${external.api.service-key}")
    private String serviceKey;


    @Transactional
    public void createProperty(CreatePropertyRequestDTO request, List<MultipartFile> images, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Property property = request.toEntity(user);

        CreateApiResponse apiInfo = fetchApiInfo(request);

        property.applyApiInfo(apiInfo);

        propertyRepository.save(property);

        propertyImageService.saveImages(property, images);
    }

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

        Page<Property> propertyPage = propertyRepository.findAllByUserIdAndIsDeletedFalse(userId, pageable);
        Page<ReadAllPropertyResponse> response = propertyPage.map(property -> {

            Auction auction = auctionRepository.findByPropertyId(property.getId()).orElse(null);
            AuctionResponse auctionResponse = (auction != null) ? AuctionResponse.from(auction) : null;

            return ReadAllPropertyResponse.from(property, auctionResponse);
        });

        return CustomPageResponse.from(response);
    }

    @Transactional(readOnly = true)
    public ReadPropertyResponse getProperty(Long propertyId) {

        Property property = propertyRepository.findByIdWithImagesAndNotDeleted(propertyId)
                .orElseThrow(() -> new CustomException(ErrorCode.PROPERTY_NOT_FOUND));

        Auction auction = auctionRepository.findByPropertyId(propertyId).orElse(null);
        AuctionStatus auctionStatus = (auction != null) ? auction.getStatus() : null;

        return ReadPropertyResponse.from(property, auctionStatus);
    }

    @Transactional
    public void updateProperty(Long propertyId, UpdatePropertyRequest request) {

        Property property = propertyRepository.findByIdWithImagesAndNotDeleted(propertyId)
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

        boolean hasNonScheduledAuction = auctionRepository.existsByPropertyIdAndStatusNotIn(propertyId, List.of(AuctionStatus.SCHEDULED, AuctionStatus.CANCELLED));

        if (hasNonScheduledAuction) {
            throw new CustomException(ErrorCode.PROPERTY_CANNOT_DELETE);
        }

        property.softDelete();
    }

    private CreateApiResponse fetchApiInfo(CreatePropertyRequestDTO request) {

        AptResponse response = aptClient.getApt(
                serviceKey,
                request.lawdCd(),
                request.dealYmd(),
                1,
                30
        );

        List<AptItem> items = response.response().body().items().item();

        if (items == null || items.isEmpty()) {
            throw new CustomException(ErrorCode.EXTERNAL_API_FAILED);
        }

        return AptMapper.toCreateApiResponse(items.get(0), request.address());
    }
}
