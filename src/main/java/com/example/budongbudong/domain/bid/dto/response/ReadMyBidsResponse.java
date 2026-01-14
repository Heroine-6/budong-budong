package com.example.budongbudong.domain.bid.dto.response;

import com.example.budongbudong.domain.bid.entity.Bid;
import com.example.budongbudong.domain.bid.enums.BidStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class ReadMyBidsResponse {

    private final Long propertyId;
    private final BidStatus status;
    private final String propertyName;
    private final Long price;
    private final LocalDateTime endedAt;

    public static ReadMyBidsResponse from(Bid bid) {
        return new ReadMyBidsResponse(
                bid.getAuction().getProperty().getId(),
                bid.getStatus(),
                bid.getAuction().getProperty().getName(),
                bid.getPrice(),
                bid.getAuction().getEndedAt()
        );
    }
}
