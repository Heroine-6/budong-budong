package com.example.budongbudong.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class ApiExplorer {

    @Value("${API_SERVICE_KEY}")
    private String serviceKey;

    private static String getData(String urlString) throws IOException {

        // 3. URL 객체 생성.
        URL url = new URL(urlString);

        // 4. 요청하고자 하는 URL과 통신하기 위한 Connection 객체 생성.
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // 5. 통신을 위한 메소드 SET.
        conn.setRequestMethod("GET");

        // 6. 통신을 위한 Content-type SET.
        conn.setRequestProperty("Content-type", "application/xml");

        // 7. 통신 응답 코드 확인.
        log.info("Response code: " + conn.getResponseCode());

        // 8. 전달받은 데이터를 BufferedReader 객체로 저장.
        BufferedReader rd;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        // 9. 저장된 데이터를 라인별로 읽어 StringBuilder 객체로 저장.
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }

        // 10. 객체 해제.
        rd.close();
        conn.disconnect();

        return sb.toString();
    }

    public String getAptTradePrice(String LAWDCode, String dealYMD, int pageNo, int numOfRows) throws IOException {

        // 1. URL을 만들기 위한 StringBuilder.
        StringBuilder urlBuilder = new StringBuilder("https://apis.data.go.kr/1613000/RTMSDataSvcAptTradeDev/getRTMSDataSvcAptTradeDev"); /*URL*/

        // 2. 오픈 API의요청 규격에 맞는 파라미터 생성, 발급받은 인증키.
        urlBuilder.append("?").append(URLEncoder.encode("serviceKey", StandardCharsets.UTF_8)).append("=").append(serviceKey);
        urlBuilder.append("&").append(URLEncoder.encode("LAWD_CD", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode(LAWDCode, StandardCharsets.UTF_8)); // 법정동 코드
        urlBuilder.append("&").append(URLEncoder.encode("DEAL_YMD", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode(dealYMD, StandardCharsets.UTF_8)); // 준공연월
        urlBuilder.append("&").append(URLEncoder.encode("pageNo", StandardCharsets.UTF_8)).append("=").append(pageNo);
        urlBuilder.append("&").append(URLEncoder.encode("numOfRows", StandardCharsets.UTF_8)).append("=").append(numOfRows);
        urlBuilder.append("&").append(URLEncoder.encode("returnType", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode("XML", StandardCharsets.UTF_8));

        return getData(urlBuilder.toString());
    }

    public String getOffiTradePrice(String LAWDCode, String dealYMD) throws IOException {

        // 1. URL을 만들기 위한 StringBuilder.
        StringBuilder urlBuilder = new StringBuilder("https://apis.data.go.kr/1613000/RTMSDataSvcOffiTrade/getRTMSDataSvcOffiTrade"); /*URL*/

        // 2. 오픈 API의요청 규격에 맞는 파라미터 생성, 발급받은 인증키.
        urlBuilder.append("?").append(URLEncoder.encode("serviceKey", StandardCharsets.UTF_8)).append("=").append(serviceKey);
        urlBuilder.append("&").append(URLEncoder.encode("LAWD_CD", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode(LAWDCode, StandardCharsets.UTF_8));
        urlBuilder.append("&").append(URLEncoder.encode("DEAL_YMD", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode(dealYMD, StandardCharsets.UTF_8));
        urlBuilder.append("&").append(URLEncoder.encode("returnType", StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode("XML", StandardCharsets.UTF_8));


        return getData(urlBuilder.toString());
    }

}