package com.example.budongbudong.domain.property.service;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.Property;
import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.common.response.CustomPageResponse;
import com.example.budongbudong.domain.auction.dto.response.AuctionResponse;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.property.client.AptClient;
import com.example.budongbudong.domain.property.client.AptItem;
import com.example.budongbudong.domain.property.client.AptMapper;
import com.example.budongbudong.domain.property.client.AptResponse;
import com.example.budongbudong.domain.property.dto.condition.SearchPropertyCond;
import com.example.budongbudong.domain.property.dto.request.CreatePropertyRequest;
import com.example.budongbudong.domain.property.dto.request.UpdatePropertyRequest;
import com.example.budongbudong.domain.property.dto.response.CreateApiResponse;
import com.example.budongbudong.domain.property.dto.response.ReadAllPropertyResponse;
import com.example.budongbudong.domain.property.dto.response.ReadPropertyResponse;
import com.example.budongbudong.domain.property.repository.PropertyRepository;
import com.example.budongbudong.domain.propertyimage.service.PropertyImageService;
import com.example.budongbudong.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
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
    public void createProperty(CreatePropertyRequest request, List<MultipartFile> images, Long userId) {

        User user = userRepository.getByIdOrThrow(userId);

        // 입력 받은 값을 통해 엔티티 생성
        Property property = request.toEntity(user);

        // 외부 API에서 받은 값
        CreateApiResponse apiInfo = fetchApiInfo(request);

        // 외부 API에서 받은 값 엔티티에 저장
        property.applyApiInfo(apiInfo);

        // DB에 저장
        propertyRepository.save(property);

        propertyImageService.saveImages(property, images);
    }

    @Transactional(readOnly = true)
    public CustomPageResponse<ReadAllPropertyResponse> getAllPropertyList(
            SearchPropertyCond cond,
            Pageable pageable
    ) {
        long start = System.currentTimeMillis();
        Long min = cond.getMinPrice();
        Long max = cond.getMaxPrice();
        if(min != null && max != null && min > max) {
            throw new CustomException(ErrorCode.INVALID_PRICE_RANGE);
        }

        Page<ReadAllPropertyResponse> page = propertyRepository.searchProperties(cond, pageable);

        long end = System.currentTimeMillis();
        log.info("[매물 검색] 실행시간:{}, cond={}", end - start, cond);
        return CustomPageResponse.from(page);
    }

    @Transactional(readOnly = true)
    public CustomPageResponse<ReadAllPropertyResponse> getMyPropertyList(Long userId, Pageable pageable) {

        Page<Property> propertyPage = propertyRepository.findAllByUserIdAndIsDeletedFalse(userId, pageable);

        Page<ReadAllPropertyResponse> response = getReadAllPropertyResponses(propertyPage);

        return CustomPageResponse.from(response);
    }

    @Transactional(readOnly = true)
    public ReadPropertyResponse getProperty(Long propertyId) {

        Property property = propertyRepository.getByIdWithImagesAndNotDeletedOrThrow(propertyId);

        AuctionStatus auctionStatus = auctionRepository.findStatusByPropertyIdOrNull(propertyId);

        return ReadPropertyResponse.from(property, auctionStatus);
    }

    @Transactional
    public void updateProperty(Long propertyId, UpdatePropertyRequest request, Long userId) {

        userRepository.getByIdOrThrow(userId);

        Property property = propertyRepository.getByIdWithImagesAndNotDeletedOrThrow(propertyId);

        property.update(
                request.getPrice(),
                request.getMigrateDate(),
                request.getDescription()
        );
    }

    @Transactional
    public void deleteProperty(Long propertyId, Long userId) {

        userRepository.getByIdOrThrow(userId);

        Property property = propertyRepository.getByIdAndNotDeletedOrThrow(propertyId);

        auctionRepository.validatePropertyDeletableOrThrow(propertyId);

        property.softDelete();
    }

    private CreateApiResponse fetchApiInfo(CreatePropertyRequest request) {

        // aptClient를 통해 외부 API 요청 (전체 응답 형식)
        AptResponse response = aptClient.getApt(
                serviceKey,
                request.lawdCd(),
                request.dealYmd(),
                1,
                30
        );

        // 전체 응답 중 필요한 값만 꺼내 저장
        List<AptItem> items = response.response().body().items().item();

        if (items == null || items.isEmpty()) {
            throw new CustomException(ErrorCode.EXTERNAL_API_FAILED);
        }

        // aptMapper를 통해 필요한 형태의 값으로 변환하여 반환
        return AptMapper.toCreateApiResponse(items.get(0), request.address());
    }

    private @NonNull Page<ReadAllPropertyResponse> getReadAllPropertyResponses(Page<Property> propertyPage) {

        return propertyPage.map(property -> {

            Auction auction = auctionRepository.findByPropertyIdOrNull(property.getId());
            AuctionResponse auctionResponse = (auction != null) ? AuctionResponse.from(auction) : null;

            return ReadAllPropertyResponse.from(property, auctionResponse);
        });
    }
}
