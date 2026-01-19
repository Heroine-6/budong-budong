package com.example.budongbudong.domain.bid.dto.response;

import com.example.budongbudong.common.entity.Bid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateBidResponse {

    private final Long bidId;
    private final Long auctionId;
    private final Long price;

    public static CreateBidResponse from(Bid bid) {
        return new CreateBidResponse(
                bid.getId(),
                bid.getAuction().getId(),
                bid.getPrice()
        );
    }
}
