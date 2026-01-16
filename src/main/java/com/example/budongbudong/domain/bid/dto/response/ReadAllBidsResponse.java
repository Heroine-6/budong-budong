package com.example.budongbudong.domain.bid.dto.response;

import com.example.budongbudong.domain.bid.entity.Bid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import static com.example.budongbudong.common.utils.TimeFormatUtil.formatTime;

@Getter
@RequiredArgsConstructor
public class ReadAllBidsResponse {

    private final Long price;
    private final String timeFormatted;
    private final LocalDateTime createdAt;

    public static ReadAllBidsResponse from(Bid bid) {
        return new ReadAllBidsResponse(
                bid.getPrice(),
                formatTime(bid.getCreatedAt()),
                bid.getCreatedAt()
        );
    }
}
