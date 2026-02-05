package com.example.budongbudong.domain.payment.log.entity;

import com.example.budongbudong.common.entity.BaseEntity;
import com.example.budongbudong.domain.payment.enums.PaymentStatus;
import com.example.budongbudong.domain.payment.log.enums.LogType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "payment_log")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="payment_id", nullable = false)
    private Long paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name="previous_status",columnDefinition = "varchar(50)")
    private PaymentStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name="current_status",columnDefinition = "varchar(50)")
    private PaymentStatus currentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name="event_type", nullable = false,columnDefinition = "varchar(50)")
    private LogType logType;

    @Column(columnDefinition = "text")
    private String errorMessage;

    public static PaymentLog create(
            Long paymentId,
            PaymentStatus previousStatus,
            PaymentStatus currentStatus,
            LogType logType,
            String errorMessages
    ) {
        PaymentLog log = new PaymentLog();
        log.paymentId = paymentId;
        log.previousStatus = previousStatus;
        log.currentStatus = currentStatus;
        log.logType = logType;
        log.errorMessage = errorMessages;
        return log;
    }
}
