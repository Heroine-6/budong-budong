package com.example.budongbudong.domain.property.document;

import com.example.budongbudong.domain.property.enums.PropertyType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.time.*;

/**
 * Property 검색 전용 Elasticsearch Document
 * - 매물 검색 성능을 위해 JPA Entity와 분리된 검색 전용 모델
 * - 검색/필터링/정렬에 필요한 필드만 포함
 * - DB 정합성은 보장하지 않으며 조회 용도로만 사용
 */
@Getter
@Document(indexName = "property_search")
@Setting(settingPath = "elasticsearch/settings.json")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PropertySearchDocument {

    @Id
    private Long id;

    // --- 검색 + 응답 ---
    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String name;

    @Field(type = FieldType.Text, analyzer = "nori_analyzer")
    private String address;

    @Field(type = FieldType.Keyword)
    private PropertyType type;

    @Field(type = FieldType.Integer)
    private Integer builtYear;

    @Field(type = FieldType.Date)
    private LocalDate migrateDate;

    // --- 응답 전용 ( 검색 대상 아님) ---
    @Field(type = FieldType.Keyword, index = false)
    private String description;

    @Field(type = FieldType.Double, index = false)
    private BigDecimal supplyArea;

    @Field(type = FieldType.Double, index = false)
    private BigDecimal privateArea;

    @Field(type = FieldType.Keyword, index = false)
    private String thumbnailImage;

    // --- 검색/정렬 ---
    @Field(type = FieldType.Long)
    private Long price;

    // --- 경매 요약 (리스트용) ---
    @Field(type = FieldType.Object)
    private AuctionSummary auction;
}

