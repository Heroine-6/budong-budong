package com.example.budongbudong.domain.payment.MQ;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequestedMQEvent {
    private Long paymentId;
}
