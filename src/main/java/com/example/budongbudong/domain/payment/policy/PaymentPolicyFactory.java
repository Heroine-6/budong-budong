package com.example.budongbudong.domain.payment.policy;

import com.example.budongbudong.domain.payment.enums.PaymentType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * PaymentType 기반 정책 전략 선택 팩토리
 */
@Component
public class PaymentPolicyFactory {

    private final Map<PaymentType, PaymentPolicy> policyMap;

    public PaymentPolicyFactory(List<PaymentPolicy> policies) {
        this.policyMap = policies.stream()
                .collect(Collectors.toMap(
                        PaymentPolicy::supports,
                        Function.identity()
                ));
    }

    public PaymentPolicy get(PaymentType type) {
        return policyMap.get(type);
    }
}
