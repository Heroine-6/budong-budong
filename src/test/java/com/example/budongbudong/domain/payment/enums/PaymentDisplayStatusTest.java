package com.example.budongbudong.domain.payment.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PaymentDisplayStatus 매핑 회귀 테스트")
class PaymentDisplayStatusTest {

    @Test
    @DisplayName("SUCCESS → PAID 매핑")
    void success_to_paid() {
    }

    @Test
    @DisplayName("VERIFYING → IN_PROGRESS 매핑")
    void verifying_to_in_progress() {
    }

    @Test
    @DisplayName("FAIL → FAILED 매핑")
    void fail_to_failed() {
    }

    @Test
    @DisplayName("REFUND_REQUESTED → REFUND_IN_PROGRESS 매핑")
    void refund_requested_to_refund_in_progress() {
    }

    @Test
    @DisplayName("REFUNDED → REFUNDED 매핑")
    void refunded_to_refunded() {
    }

    @Test
    @DisplayName("모든 PaymentStatus 값에 대해 매핑이 존재한다 (누락 방지)")
    void all_status_mapped() {
    }
}
