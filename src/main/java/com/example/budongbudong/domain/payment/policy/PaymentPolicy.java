package com.example.budongbudong.domain.payment.policy;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.AuctionWinner;
import com.example.budongbudong.domain.payment.enums.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PaymentPolicy {

    PaymentType supports();
    PaymentInfo assembleInfo(Auction auction, AuctionWinner winner, Long userId);

}

