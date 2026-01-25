package com.example.budongbudong.domain.bid.dto.response;

import com.example.budongbudong.domain.bid.enums.BidStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateBidMessageResponse {
    private final BidStatus bidStatus;
    private final String message;

    public static CreateBidMessageResponse from(BidStatus bidStatus, String message) {
        return new CreateBidMessageResponse(
                bidStatus,
                message
        );
    }
}
