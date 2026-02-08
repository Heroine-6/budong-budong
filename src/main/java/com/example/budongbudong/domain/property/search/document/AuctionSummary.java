package com.example.budongbudong.domain.property.search.document;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;

/**
 * 경매 검색 결과용 요약 document
 * - 매물 검색시 함께 뿌려지는 최소한의 경매 정보
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class AuctionSummary {

    @Field(type = FieldType.Long)
    private Long auctionId;

    @Field(type = FieldType.Keyword)
    private AuctionStatus status;

    @Field(type = FieldType.Long)
    private BigDecimal startPrice;


    public static AuctionSummary from(Auction auction) {
        if (auction == null) {
            return null;
        }
        return new AuctionSummary(
                auction.getId(),
                auction.getStatus(),
                auction.getStartPrice()
        );
    }
}
