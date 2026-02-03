package com.example.budongbudong.domain.payment.toss.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TossConfirmResponse {

    private String method;
    private Card card;
    private VirtualAccount virtualAccount;
    private Transfer transfer;
    private MobilePhone mobilePhone;
    private EasyPay easyPay;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Card {
        private String issuerCode;
        private String number;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VirtualAccount {
        private String bankCode;
        private String accountNumber;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Transfer {
        private String bankCode;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MobilePhone {
        private String carrier;
        private String customerMobilePhone;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EasyPay {
        private String provider;
    }
}
