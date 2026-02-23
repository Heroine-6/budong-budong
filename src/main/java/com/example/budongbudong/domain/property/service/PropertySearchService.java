package com.example.budongbudong.domain.property.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.example.budongbudong.common.response.CustomPageResponse;
import com.example.budongbudong.common.response.CustomSliceResponse;
import com.example.budongbudong.domain.property.search.document.PropertySearchDocument;
import com.example.budongbudong.domain.property.dto.condition.SearchPropertyCond;
import com.example.budongbudong.domain.property.dto.response.SearchPropertyResponse;
import com.example.budongbudong.domain.property.search.PropertySearchQueryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PropertySearchService {

    private final ElasticsearchOperations operations;
    private final PropertySearchQueryBuilder queryBuilder;

    /**
     * Elasticsearch 기반 매물 검색
     * - count 조회 없이 Slice 방식으로 페이징 처리
     * - 검색 조건에 따라 동적 Query 생성
     */
    public CustomPageResponse<SearchPropertyResponse> search(SearchPropertyCond cond, Pageable pageable){

        // 검색 조건(SearchPropertyCond)을 기반으로 Elasticsearch Query 생성
        Query query = queryBuilder.build(cond);

        //Slice pageable
        int requestedSize = pageable.getPageSize();

        NativeQuery nativeQuery = NativeQuery.builder().withQuery(query).withTrackTotalHits(true) .withPageable(pageable).build();

        //Elasticsearch 실행
        SearchHits<PropertySearchDocument> searchHits = operations.search(nativeQuery, PropertySearchDocument.class);
        long total = searchHits.getTotalHits();

        List<SearchPropertyResponse> contents = searchHits.getSearchHits().stream()
                        .map(SearchHit::getContent)
                        .map(SearchPropertyResponse::from)
                        .toList();

        Page<SearchPropertyResponse> page = new PageImpl<>(contents, pageable, total);
        return CustomPageResponse.from(page);
    }
}

