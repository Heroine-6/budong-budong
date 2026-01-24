package com.example.budongbudong.domain.bid.dto.response;

import com.example.budongbudong.common.entity.Bid;
import com.example.budongbudong.domain.bid.enums.BidStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateBidResponse {
    private final Long bidId;
    private final Long auctionId;
    private final Long price;
    private final BidStatus bidStatus;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String message;

    public static CreateBidResponse from(Bid bid) {
        return new CreateBidResponse(
                bid.getId(),
                bid.getAuction().getId(),
                bid.getPrice(),
                bid.getStatus(),
                null
        );
    }

    public static CreateBidResponse rejectedFrom(BidStatus bidStatus, String message) {
        return new CreateBidResponse(
                null,
                null,
                null,
                bidStatus,
                message
        );
    }
}
