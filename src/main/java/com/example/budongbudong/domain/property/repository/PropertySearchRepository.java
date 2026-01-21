package com.example.budongbudong.domain.property.repository;

import com.example.budongbudong.domain.property.document.PropertySearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PropertySearchRepository extends ElasticsearchRepository<PropertySearchDocument, Long> {
}

