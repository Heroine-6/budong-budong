package com.example.budongbudong.domain.property.realdeal;

import com.example.budongbudong.BudongBudongApplication;
import com.example.budongbudong.common.entity.RealDeal;
import com.example.budongbudong.domain.property.realdeal.client.NaverGeoClient;
import com.example.budongbudong.domain.property.realdeal.client.NaverGeoResponse;
import com.example.budongbudong.domain.property.realdeal.repository.RealDealRepository;
import com.example.budongbudong.domain.property.enums.PropertyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = BudongBudongApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@TestPropertySource(
        locations = "file:.env"
)
class NaverGeoClientTest {

    @Autowired
    private NaverGeoClient naverGeoClient;

    @Autowired
    private RealDealRepository realDealRepository;

    @Test
    @DisplayName("네이버 지오코딩 단건 호출이 정상 응답을 반환한다")
    void geocode_single() {
        String address = "서울특별시 종로구 숭인동 766";
        NaverGeoResponse response = naverGeoClient.geocode(address);

        assertThat(response).isNotNull();
        assertThat(response.hasResult()).isTrue();

        NaverGeoResponse.Address addr = response.addresses().get(0);

        RealDeal deal = RealDeal.builder()
                .propertyName("테스트매물-" + System.currentTimeMillis())
                .address(address)
                .dealAmount(new BigDecimal("100000000"))
                .exclusiveArea(new BigDecimal("84.32"))
                .floor(1)
                .builtYear(2010)
                .dealDate(LocalDate.now())
                .propertyType(PropertyType.APARTMENT)
                .lawdCd("11110")
                .build();
        deal.applyGeoCode(new BigDecimal(addr.y()), new BigDecimal(addr.x()), addr.roadAddress());

        RealDeal saved = realDealRepository.save(deal);

        assertThat(saved.getId()).isNotNull();
    }
}
