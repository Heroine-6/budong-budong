package com.example.budongbudong.domain.property.realdeal.repository;

import com.example.budongbudong.domain.property.realdeal.document.RealDealDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface RealDealSearchRepository extends ElasticsearchRepository<RealDealDocument, Long> {
}
