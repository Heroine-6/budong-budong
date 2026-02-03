package com.example.budongbudong.domain.payment.utils;

import com.example.budongbudong.domain.payment.enums.PaymentMethodType;
import com.example.budongbudong.domain.payment.toss.dto.response.TossConfirmResponse;
import com.example.budongbudong.domain.payment.toss.enums.TossCardIssuer;
import org.springframework.stereotype.Component;

/**
 * 토스 승인 응답 값 코드를 매칭되는 문자열로 포매팅하는 컴포넌트
 */
@Component
public class PaymentMethodDetailFormatter {

    private PaymentMethodDetailFormatter() {
    }

    public static String format(PaymentMethodType type, TossConfirmResponse response) {
        return switch (type) {
            case CARD -> formatCard(response.getCard());
            case EASY_PAY -> formatEasyPay(response.getEasyPay());
            default -> type.getMessage();
        };
    }

    private static String formatCard(TossConfirmResponse.Card card) {
        if (card == null) return "";
        String issuerName = TossCardIssuer.nameOf(card.getIssuerCode());
        return String.format("%s %s", issuerName, card.getNumber());
    }

    private static String formatEasyPay(TossConfirmResponse.EasyPay easyPay) {
        if (easyPay == null) return "";
        return easyPay.getProvider();
    }
}
