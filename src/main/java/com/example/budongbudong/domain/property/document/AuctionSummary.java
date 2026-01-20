package com.example.budongbudong.domain.property.document;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

/**
 * 경매 검색 결과용 요약 document
 * - 매물 검색시 함께 뿌려지는 최소한의 경매 정보
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class AuctionSummary {

    @Field(type= FieldType.Long)
    private Long auctionId;

    @Field(type = FieldType.Keyword)
    private AuctionStatus status;

    @Field(type = FieldType.Long)
    private Long startPrice;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime startedAt;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime endedAt;

    public static AuctionSummary from(Auction auction){
        if (auction == null) {
            return null;
        }
        return new AuctionSummary(
                auction.getId(),
                auction.getStatus(),
                auction.getStartPrice(),
                auction.getStartedAt(),
                auction.getEndedAt()
        );
    }
}
