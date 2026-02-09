package com.example.budongbudong.domain.property.realdeal.service;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.Property;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.bid.repository.BidRepository;
import com.example.budongbudong.domain.property.enums.PropertyType;
import com.example.budongbudong.domain.property.realdeal.client.KakaoGeoClient;
import com.example.budongbudong.domain.property.realdeal.client.NaverGeoClient;
import com.example.budongbudong.domain.property.realdeal.document.RealDealDocument;
import com.example.budongbudong.domain.property.realdeal.dto.MarketCompareResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitsImpl;
import org.springframework.data.elasticsearch.core.TotalHitsRelation;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DealSearchServiceCompareTest {

    @Mock
    ElasticsearchOperations elasticsearchOperations;

    @Mock
    NaverGeoClient naverGeoClient;

    @Mock
    KakaoGeoClient kakaoGeoClient;

    @Mock
    AuctionRepository auctionRepository;

    @Mock
    BidRepository bidRepository;

    @InjectMocks
    DealSearchService dealSearchService;

    @Test
    @DisplayName("비교 API는 실거래 0건이어도 0값으로 응답한다")
    void 비교_API_실거래_0건_응답() {
        // given
        Auction auction = Auction.builder()
                .startPrice(new BigDecimal("100000000"))
                .property(Property.builder()
                        .address("서울특별시 송파구 잠실동")
                        .type(PropertyType.APARTMENT)
                        .privateArea(new BigDecimal("84.0"))
                        .latitude(new BigDecimal("37.5145"))
                        .longitude(new BigDecimal("127.1059"))
                        .build())
                .build();

        when(auctionRepository.getAuctionWithPropertyOrTrow(1L)).thenReturn(auction);
        when(bidRepository.getHighestPriceOrStartPrice(1L, auction.getStartPrice()))
                .thenReturn(auction.getStartPrice());
        when(elasticsearchOperations.search(any(), eq(RealDealDocument.class)))
                .thenReturn(new SearchHitsImpl<>(0, TotalHitsRelation.EQUAL_TO, 0f, Duration.ZERO,
                        null, null, List.of(), null, null, null));

        // when
        MarketCompareResponse response = dealSearchService.compareWithAuction(1L, 1.0, 50, null);

        // then
        assertThat(response.getTotalCount()).isEqualTo(0);
        assertThat(response.getMedianPricePerArea()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.getDeals()).isEmpty();
        assertThat(response.getStartPriceRatio()).isEqualTo(0);
        assertThat(response.getHighestBidPriceRatio()).isEqualTo(0);
        assertThat(response.getInputPriceRatio()).isEqualTo(0);
    }
}
