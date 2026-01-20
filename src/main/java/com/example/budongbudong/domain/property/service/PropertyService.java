package com.example.budongbudong.domain.property.service;

import com.example.budongbudong.common.entity.*;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.common.response.CustomPageResponse;
import com.example.budongbudong.domain.auction.dto.response.AuctionResponse;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.property.client.AptClient;
import com.example.budongbudong.domain.property.client.AptItem;
import com.example.budongbudong.domain.property.client.OffiClient;
import com.example.budongbudong.domain.property.client.VillaClient;
import com.example.budongbudong.domain.property.dto.request.CreatePropertyRequest;
import com.example.budongbudong.domain.property.dto.request.UpdatePropertyRequest;
import com.example.budongbudong.domain.property.enums.PropertyType;
import com.example.budongbudong.domain.property.lawdcode.LawdCodeService;
import com.example.budongbudong.domain.property.client.AptMapper;
import com.example.budongbudong.domain.property.client.AptResponse;
import com.example.budongbudong.domain.property.dto.condition.SearchPropertyCond;
import com.example.budongbudong.domain.property.dto.request.CreatePropertyRequest;
import com.example.budongbudong.domain.property.dto.request.UpdatePropertyRequest;
import com.example.budongbudong.domain.property.dto.response.*;
import com.example.budongbudong.domain.property.dto.response.CreateApiResponse;
import com.example.budongbudong.domain.property.dto.response.ReadAllPropertyResponse;
import com.example.budongbudong.domain.property.dto.response.ReadPropertyResponse;
import com.example.budongbudong.domain.property.repository.PropertyRepository;
import com.example.budongbudong.domain.propertyimage.service.PropertyImageService;
import com.example.budongbudong.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final PropertyImageService propertyImageService;
    private final AuctionRepository auctionRepository;
    private final AptClient aptClient;
    private final OffiClient offiClient;
    private final VillaClient villaClient;
    private final LawdCodeService lawdCodeService;

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
    public CustomPageResponse<ReadAllPropertyResponse> getAllPropertyList(SearchPropertyCond cond, Pageable pageable) {

        Long min = cond.getMinPrice();
        Long max = cond.getMaxPrice();
        if(min != null && max != null && min > max) {
            throw new CustomException(ErrorCode.INVALID_PRICE_RANGE);
        }

        Page<Property> page =
                propertyRepository.findAllByIsDeletedFalse(pageable);

        Page<ReadAllPropertyResponse> response =
                page.map(property -> {
                    Auction auction =
                            auctionRepository.findByPropertyIdOrNull(property.getId());
                    AuctionResponse auctionResponse =
                            auction != null ? AuctionResponse.from(auction) : null;
                    return ReadAllPropertyResponse.from(property, auctionResponse);
                });

        return CustomPageResponse.from(response);
    }

    @Transactional(readOnly = true)
    public CustomPageResponse<ReadAllPropertyResponse> getMyPropertyList(Long userId, Pageable pageable) {

        Page<ReadAllPropertyResponse> page = propertyRepository.findAllMyProperties(userId, pageable);
        return CustomPageResponse.from(page);
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

        // 주소에서 지역코드(LAWD_CD) 자동 추출
        String lawdCd = lawdCodeService.getLawdCdFromAddress(request.address())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_ADDRESS));
        log.info("추출된 법정동 코드: {} (주소: {})", lawdCd, request.address());

        // PropertyType(아파트,빌라,오피스텔)에 따라 적절한 API 호출
        AptResponse response = fetchFromApi(request.type(), lawdCd, request.dealYmd());

        // 전체 응답 중 필요한 값만 꺼내 저장
        List<AptItem> items = response.response().body().items().item();

        if (items == null || items.isEmpty()) {
            throw new CustomException(ErrorCode.EXTERNAL_API_FAILED);
        }

        // 주소 + 층수로 필터링하여 정확한 매물 찾기
        AptItem matchedItem = findMatchingItem(items, request.address(), request.floor());

        // aptMapper를 통해 필요한 형태의 값으로 변환하여 반환
        return AptMapper.toCreateApiResponse(matchedItem, request.address());
    }

    // 외부 api에서 가져올 결과 값 최대 개수 (어떤 타입이든 500개씩 가져오기로 함. 많이 가져올 수록 느려짐 )
    private AptResponse fetchFromApi(PropertyType type, String lawdCd, String dealYmd) {
        return switch (type) {
            case OFFICETEL -> offiClient.getOffi(serviceKey, lawdCd, dealYmd, 1, 500);
            case VILLA -> villaClient.getVilla(serviceKey, lawdCd, dealYmd, 1, 500);
            case APARTMENT -> aptClient.getApt(serviceKey, lawdCd, dealYmd, 1, 500);
        };
    }

    private AptItem findMatchingItem(List<AptItem> items, String address, Integer targetFloor) {
        log.info("입력 주소: {}, 층수: {}", address, targetFloor);

        // 매칭 조건 (우선순위 순서)
        Predicate<AptItem> matchesUmdAndJibun = item ->
                item.umdNm() != null && address.contains(item.umdNm())
                        && item.jibun() != null && matchesJibun(address, item.jibun());

        Predicate<AptItem> matchesJibunOnly = item ->
                item.jibun() != null && matchesJibun(address, item.jibun());

        Predicate<AptItem> matchesUmdOnly = item ->
                item.umdNm() != null && address.contains(item.umdNm());

        // 우선순위대로 매칭 시도
        List<AptItem> candidates = findCandidates(items, matchesUmdAndJibun);
        if (candidates.isEmpty()) {
            candidates = findCandidates(items, matchesJibunOnly);
        }
        if (candidates.isEmpty()) {
            candidates = findCandidates(items, matchesUmdOnly);
        }

        if (candidates.isEmpty()) {
            log.warn("매칭된 매물 없음 - 주소: {}", address);
            throw new CustomException(ErrorCode.API_PROPERTY_NOT_MATCHED);
        }

        // 층수가 가장 비슷한 거래 선택
        AptItem bestMatch = selectBestMatchByFloor(candidates, targetFloor);

        log.info("선택된 매물: {} | 층수: {} | 가격: {}", bestMatch.getName(), bestMatch.floor(), bestMatch.dealAmount());
        return bestMatch;
    }

    private List<AptItem> findCandidates(List<AptItem> items, Predicate<AptItem> condition) {
        return items.stream()
                .filter(condition)
                .toList();
    }

    private AptItem selectBestMatchByFloor(List<AptItem> candidates, Integer targetFloor) {
        if (targetFloor == null) {
            return candidates.get(0);
        }

        return candidates.stream()
                .filter(item -> item.floor() != null)
                .min(Comparator.comparingInt(item -> Math.abs(item.floor() - targetFloor)))
                .orElse(candidates.get(0));
    }

    private boolean matchesJibun(String address, String jibun) {
        // 지번을 정확히 매칭 (단어 경계로)
        // "53"이 "홍익동 53"에서 매칭, "153"이나 "531"은 안 됨
        return address.endsWith(" " + jibun)
                || address.endsWith(jibun)
                || address.contains(" " + jibun + "-")
                || address.contains(" " + jibun + " ");
    }

}
