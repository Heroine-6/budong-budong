package com.example.budongbudong.domain.payment.toss.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum TossCardIssuer {

    KOOKMIN("11", "국민"),
    HANA("21", "하나"),
    TOSSBANK("24", "토스뱅크"),
    GIUP_BC("3K", "기업비씨"),
    KBANK("3A", "케이뱅크"),
    BC("31", "BC"),
    WOORI("33", "우리"),
    SUHYUP("34", "수협"),
    JEONBUK("35", "전북"),
    CITI("36", "씨티"),
    POST("37", "우체국"),
    SAEMAEUL("38", "새마을"),
    SAVINGS("39", "저축"),
    SHINHAN("41", "신한"),
    JEJU("42", "제주"),
    GWANGJU("46", "광주"),
    SAMSUNG("51", "삼성"),
    HYUNDAI("61", "현대"),
    SHINHYUP("62", "신협"),
    LOTTE("71", "롯데"),
    NONGHYUP("91", "농협"),
    WOORI_W1("W1", "우리"),
    JCB("4J", "JCB"),
    MASTER("4M", "마스터"),
    VISA("4V", "비자"),
    DINERS("6D", "다이너스"),
    AMEX("7A", "아메리칸익스프레스"),
    UNIONPAY("3C", "유니온페이");

    private final String code;
    private final String name;

    private static final Map<String, TossCardIssuer> CODE_MAP =
            Stream.of(values()).collect(Collectors.toMap(TossCardIssuer::getCode, e -> e));

    public static String nameOf(String code) {
        TossCardIssuer issuer = CODE_MAP.get(code);
        return issuer != null ? issuer.name : code;
    }
}
