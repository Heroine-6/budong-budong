package com.example.budongbudong.domain.property.realdeal.dto;

import com.example.budongbudong.domain.property.realdeal.document.RealDealDocument;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RealDealSearchResponse {

    private final long totalCount;
    private final List<RealDealDocument> deals;

    public static RealDealSearchResponse of(List<RealDealDocument> deals) {
        return RealDealSearchResponse.builder()
                .totalCount(deals.size())
                .deals(deals)
                .build();
    }
}
