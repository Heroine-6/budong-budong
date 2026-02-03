package com.example.budongbudong.domain.property.realdeal.service;

import com.example.budongbudong.common.entity.RealDeal;
import com.example.budongbudong.domain.property.client.*;
import com.example.budongbudong.domain.property.enums.PropertyType;
import com.example.budongbudong.domain.property.lawdcode.LawdCodeService;
import com.example.budongbudong.domain.property.realdeal.repository.RealDealRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 국토교통부 공공데이터 실거래가 수집 서비스
 *
 * - 아파트, 오피스텔, 빌라 실거래가 데이터를 API로 수집
 * - 법정동 코드(LAWD_CD)와 기간으로 조회
 * - 중복 데이터는 unique constraint로 자동 스킵
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DealCollectService {

    private final AptClient aptClient;
    private final OffiClient offiClient;
    private final VillaClient villaClient;
    private final RealDealRepository realDealRepository;
    private final LawdCodeService lawdCodeService;

    @Value("${external.api.service-key}")
    private String serviceKey;

    private static final int NUM_OF_ROWS = 1000;  // API 페이지당 최대 건수
    private static final DateTimeFormatter DEAL_YMD_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");

    /**
     * 지정된 법정동 코드와 기간에 해당하는 실거래가 데이터 수집
     * @param lawdCd 법정동 코드 (예: "11110" = 종로구)
     * @param from 시작 월
     * @param to 종료 월
     */
    public void collect(String lawdCd, YearMonth from, YearMonth to) {
        YearMonth current = from;
        while (!current.isAfter(to)) {
            String dealYmd = current.format(DEAL_YMD_FORMAT);
            for (PropertyType type : PropertyType.values()) {
                collectByType(lawdCd, dealYmd, type);
            }
            current = current.plusMonths(1);
        }
    }

    @Transactional
    public void saveItems(List<AptItem> items, PropertyType type, String lawdCd, String dealYmd) {
        int saved = 0;
        for (AptItem item : items) {
            try {
                RealDeal deal = toEntity(item, type, lawdCd, dealYmd);
                realDealRepository.save(deal);
                saved++;
            } catch (DataIntegrityViolationException e) {
                log.debug("[중복 스킵] name={}, lawdCd={}", item.getName(), lawdCd);
            }
        }
    }

    private void collectByType(String lawdCd, String dealYmd, PropertyType type) {
        int pageNo = 1;
        while (true) {
            try {
                AptResponse response = fetchFromApi(type, lawdCd, dealYmd, pageNo);
                List<AptItem> items = extractItems(response);
                if (items == null || items.isEmpty()) break;

                saveItems(items, type, lawdCd, dealYmd);

                if (items.size() < NUM_OF_ROWS) break;
                pageNo++;
            } catch (Exception e) {
                log.error("[수집 실패] lawdCd={}, dealYmd={}, type={}, page={}",
                        lawdCd, dealYmd, type, pageNo, e);
                break;
            }
        }
    }

    private AptResponse fetchFromApi(PropertyType type, String lawdCd, String dealYmd, int pageNo) {
        return switch (type) {
            case APARTMENT -> aptClient.getApt(serviceKey, lawdCd, dealYmd, pageNo, NUM_OF_ROWS);
            case OFFICETEL -> offiClient.getOffi(serviceKey, lawdCd, dealYmd, pageNo, NUM_OF_ROWS);
            case VILLA -> villaClient.getVilla(serviceKey, lawdCd, dealYmd, pageNo, NUM_OF_ROWS);
        };
    }

    private List<AptItem> extractItems(AptResponse response) {
        if (response == null || response.response() == null
                || response.response().body() == null
                || response.response().body().items() == null) {
            return null;
        }
        return response.response().body().items().item();
    }



    private RealDeal toEntity(AptItem item, PropertyType type, String lawdCd, String dealYmd) {
        String address = buildAddress(item, lawdCd);
        BigDecimal dealAmount = parseDealAmount(item.dealAmount());
        BigDecimal area = parseArea(item.excluUseAr());
        LocalDate dealDate = parseDealDate(dealYmd);

        return RealDeal.builder()
                .propertyName(item.getName() != null ? item.getName() : "Unknown")
                .address(address)
                .dealAmount(dealAmount)
                .exclusiveArea(area)
                .floor(item.floor())
                .builtYear(item.buildYear() != null ? item.buildYear().getValue() : null)
                .dealDate(dealDate)
                .propertyType(type)
                .lawdCd(lawdCd)
                .build();
    }

    private String buildAddress(AptItem item, String lawdCd) {
        String prefix = lawdCodeService.getAddressPrefixFromLawdCd(lawdCd).orElse("");
        String umdNm = item.umdNm() != null ? item.umdNm() : "";
        String jibun = item.jibun() != null ? " " + item.jibun() : "";
        String localPart = (umdNm + jibun).trim();

        if (prefix.isEmpty()) {
            return localPart;
        }
        return (prefix + " " + localPart).trim();
    }

    private BigDecimal parseDealAmount(String dealAmount) {
        if (dealAmount == null) return BigDecimal.ZERO;
        String normalized = dealAmount.replace(",", "").replace(" ", "");
        if (normalized.isBlank()) return BigDecimal.ZERO;
        return new BigDecimal(normalized).multiply(BigDecimal.valueOf(10000));
    }

    private BigDecimal parseArea(String area) {
        if (area == null || area.isBlank()) return null;
        return new BigDecimal(area.trim());
    }

    private LocalDate parseDealDate(String dealYmd) {
        return LocalDate.of(
                Integer.parseInt(dealYmd.substring(0, 4)),
                Integer.parseInt(dealYmd.substring(4, 6)),
                1
        );
    }
}
