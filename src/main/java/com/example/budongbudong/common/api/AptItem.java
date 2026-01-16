package com.example.budongbudong.common.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Year;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AptItem(
        @JsonProperty("aptNm") String aptNm,
        @JsonProperty("dealAmount") String dealAmount,
        @JsonProperty("excluUseAr") String excluUseAr,
        @JsonProperty("buildYear") Year buildYear,
        @JsonProperty("umdNm") String umdNm,
        @JsonProperty("jibun") String jibun
) {}