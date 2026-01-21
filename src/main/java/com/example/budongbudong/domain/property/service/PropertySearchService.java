package com.example.budongbudong.domain.property.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.example.budongbudong.common.response.CustomSliceResponse;
import com.example.budongbudong.domain.property.document.PropertySearchDocument;
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
    public CustomSliceResponse<SearchPropertyResponse> search(SearchPropertyCond cond, Pageable pageable){

        long t0 = System.currentTimeMillis();

        // 검색 조건(SearchPropertyCond)을 기반으로 Elasticsearch Query 생성
        Query query = queryBuilder.build(cond);
        long t1 = System.currentTimeMillis();

        //Slice pageable
        int requestedSize = pageable.getPageSize();
        Pageable slicePageable = PageRequest.of(pageable.getPageNumber(), requestedSize + 1, pageable.getSort());

        NativeQuery nativeQuery = NativeQuery.builder().withQuery(query).withPageable(slicePageable).build();

        //Elasticsearch 실행
        SearchHits<PropertySearchDocument> searchHits = operations.search(nativeQuery, PropertySearchDocument.class);
        long t2 = System.currentTimeMillis();

        // SearchHits에서 실제 Document만 추출
        List<PropertySearchDocument> documents = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();

        boolean hasNext = documents.size() > requestedSize;
        if (hasNext) {
            documents = documents.subList(0, requestedSize);
        }

        List<SearchPropertyResponse> content = documents.stream().map(SearchPropertyResponse::from).toList();

        long t3 = System.currentTimeMillis();

        log.info("[PropertySearch] build={}ms, es={}ms, map={}ms, total={}ms, hits={}, cond={}",
                (t1 - t0),
                (t2 - t1),
                (t3 - t2),
                (t3 - t0),
                searchHits.getTotalHits(),
                cond
        );

        return CustomSliceResponse.from(content,pageable.getPageSize(), pageable.getPageNumber(), hasNext);
    }
}

