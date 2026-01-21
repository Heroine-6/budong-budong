package com.example.budongbudong.domain.property.search;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.property.dto.condition.SearchPropertyCond;
import com.example.budongbudong.domain.property.enums.PropertyType;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.Year;

/**
 * Property 검색용 Elasticsearch Query Builder
 * - {@link SearchPropertyCond}를 Elasticsearch QueryDsl로 변환
 * - 검색(must)와 필터(filter) 조건을 분리
 * - 조건이 없는 경우 Query에 포함하지 않음
 * - 검색 실행 책임은 Service로
 */
@Component
public class PropertySearchQueryBuilder {

    public Query build(SearchPropertyCond cond){
        return Query.of(q->q.bool(b-> {
            //must (검색)
            Query name = nameContains(cond.getName());
            if (name != null) b.must(name);

            Query address = addressContains(cond.getAddress());
            if (address != null) b.must(address);

            // filter (정확/범위/고정조건)
            Query type = typeEq(cond.getType());
            if (type != null) b.filter(type);

            Query price = priceRange(cond);
            if (price != null) b.filter(price);

            Query migrateDate = migrateDateFrom(cond.getMigrateDate());
            if (migrateDate != null) b.filter(migrateDate);

            Query builtYear = builtYearFrom(cond.getBuiltYear());
            if (builtYear != null) b.filter(builtYear);

            Query status = auctionStatusEq(cond.getStatus());
            if (status != null) b.filter(status);

            return b;
        }));
    }

    private Query nameContains(String name){
        return StringUtils.hasText(name)
                ? Query.of(q -> q.match(m -> m.field("name").query(name)))
                : null;
    }

    private Query typeEq(PropertyType type){
        return type != null
                ? Query.of(q->q.term(t->t.field("type").value(type.name())))
                : null;
    }

    private Query addressContains(String address){
        return StringUtils.hasText(address)
                ? Query.of(q -> q.term(m -> m.field("address").value(address)))
                : null;
    }

    private Query auctionStatusEq(AuctionStatus status) {
        return status != null
                ? Query.of(q -> q.term(t -> t
                .field("auction.status")
                .value(status.name())
        ))
                : null;
    }

    private Query priceRange(SearchPropertyCond cond) {
        if (cond.getMinPrice() == null && cond.getMaxPrice() == null) {
            return null;
        }

        return Query.of(q -> q.range(r -> r
                .number(n -> {
                    n.field("price");
                    if(cond.getMinPrice() != null) {
                        n.gte(cond.getMinPrice().doubleValue());
                    }
                    if(cond.getMaxPrice() != null) {
                        n.lte(cond.getMaxPrice().doubleValue());
                    }
                    return n;
                })
        ));
    }

    private Query migrateDateFrom(LocalDate migrateDate) {
        return migrateDate != null
                ? Query.of(q -> q.range(r -> r
                .date(d -> d.field("migrateDate").gte(migrateDate.toString()))))
                : null;
    }

    private Query builtYearFrom (Year builtYear) {
        return builtYear != null
                ? Query.of(q -> q.range(r -> r.number(n -> n.field("builtYear").gte((double) builtYear.getValue()))))
                : null;
    }
}
