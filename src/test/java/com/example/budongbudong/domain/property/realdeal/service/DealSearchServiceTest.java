package com.example.budongbudong.domain.property.realdeal.service;

import co.elastic.clients.elasticsearch._types.SortOptions;
import com.example.budongbudong.domain.property.realdeal.client.KakaoGeoClient;
import com.example.budongbudong.domain.property.realdeal.client.NaverGeoClient;
import com.example.budongbudong.domain.property.realdeal.document.RealDealDocument;
import com.example.budongbudong.domain.property.realdeal.enums.DealSortType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitsImpl;
import org.springframework.data.elasticsearch.core.TotalHitsRelation;
import org.springframework.data.elasticsearch.core.query.Query;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DealSearchServiceTest {

    @Mock
    KakaoGeoClient kakaoGeoClient;

    @Mock
    NaverGeoClient naverGeoClient;

    @Mock
    ElasticsearchOperations elasticsearchOperations;

    @InjectMocks
    DealSearchService dealSearchService;

    @Test
    @DisplayName("평단가 정렬 스크립트는 분모 0/누락을 안전하게 처리한다")
    void 평단가_정렬_스크립트_안전성_검증() {
        // given
        when(elasticsearchOperations.search(any(Query.class), eq(RealDealDocument.class)))
                .thenReturn(new SearchHitsImpl<>(0, TotalHitsRelation.EQUAL_TO, 0f, Duration.ZERO, null, null, List.of(), null, null, null));

        // when
        dealSearchService.findNearby(37.5, 127.0, 1.0, 10,
                null, null, null, null, null, DealSortType.PRICE_PER_AREA_ASC);

        // then
        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(elasticsearchOperations).search(queryCaptor.capture(), eq(RealDealDocument.class));

        Query captured = queryCaptor.getValue();
        assertThat(captured).isInstanceOf(NativeQuery.class);

        NativeQuery nativeQuery = (NativeQuery) captured;
        List<SortOptions> sorts = nativeQuery.getSortOptions();

        assertThat(sorts).isNotEmpty();
        assertThat(sorts.get(0).isScript()).isTrue();
        assertThat(sorts.get(0).script().script().source())
                .contains("doc['exclusiveArea'].size()==0 || doc['exclusiveArea'].value==0 ? 0 : doc['dealAmount'].value / doc['exclusiveArea'].value");
    }
}
