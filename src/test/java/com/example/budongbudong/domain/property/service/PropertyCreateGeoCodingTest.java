package com.example.budongbudong.domain.property.service;

import com.example.budongbudong.common.entity.Property;
import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.domain.property.client.AptClient;
import com.example.budongbudong.domain.property.client.AptItem;
import com.example.budongbudong.domain.property.client.AptResponse;
import com.example.budongbudong.domain.property.client.OffiClient;
import com.example.budongbudong.domain.property.client.VillaClient;
import com.example.budongbudong.domain.property.dto.request.CreatePropertyRequest;
import com.example.budongbudong.domain.property.enums.PropertyType;
import com.example.budongbudong.domain.property.event.PropertyEventPublisher;
import com.example.budongbudong.domain.property.lawdcode.LawdCodeService;
import com.example.budongbudong.domain.property.realdeal.client.KakaoGeoClient;
import com.example.budongbudong.domain.property.realdeal.client.KakaoGeoResponse;
import com.example.budongbudong.domain.property.realdeal.client.NaverGeoClient;
import com.example.budongbudong.domain.property.realdeal.client.NaverGeoResponse;
import com.example.budongbudong.domain.property.repository.PropertyRepository;
import com.example.budongbudong.domain.propertyimage.service.PropertyImageService;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.user.enums.UserRole;
import com.example.budongbudong.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "external.api.service-key=test"
})
class PropertyCreateGeoCodingTest {

    @Autowired
    private PropertyService propertyService;

    @MockitoBean
    private PropertyRepository propertyRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PropertyImageService propertyImageService;

    @MockitoBean
    private AuctionRepository auctionRepository;

    @MockitoBean
    private AptClient aptClient;

    @MockitoBean
    private OffiClient offiClient;

    @MockitoBean
    private VillaClient villaClient;

    @MockitoBean
    private LawdCodeService lawdCodeService;

    @MockitoBean
    private PropertyEventPublisher propertyEventPublisher;

    @MockitoBean
    private NaverGeoClient naverGeoClient;

    @MockitoBean
    private KakaoGeoClient kakaoGeoClient;

    @Test
    @DisplayName("매물 등록 시 지오코딩 결과가 위도/경도로 저장된다")
    void createProperty_appliesGeoCode() {
        String address = "서울특별시 종로구 숭인동 766";

        CreatePropertyRequest request = new CreatePropertyRequest(
                "202401",
                address,
                3,
                20,
                3,
                new BigDecimal("84.32"),
                LocalDate.of(2024, 2, 1),
                "설명",
                PropertyType.APARTMENT
        );

        User user = User.create(
                "test@example.com",
                "테스트",
                "pw",
                "01012345678",
                "서울",
                UserRole.GENERAL
        );

        when(userRepository.getByIdOrThrow(any())).thenReturn(user);
        when(lawdCodeService.getLawdCdFromAddress(anyString())).thenReturn(Optional.of("11110"));

        AptItem item = new AptItem(
                "테스트아파트",
                null,
                null,
                "100,000",
                "84.32",
                Year.of(2010),
                "숭인동",
                "766",
                3
        );
        AptResponse response = new AptResponse(
                new AptResponse.Response(
                        new AptResponse.Response.Body(
                                new AptResponse.Response.Body.Items(List.of(item))
                        )
                )
        );
        when(aptClient.getApt(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(response);

        NaverGeoResponse geoResponse = new NaverGeoResponse(
                "OK",
                new NaverGeoResponse.Meta(1),
                List.of(new NaverGeoResponse.Address(
                        "서울특별시 종로구 숭인동 766",
                        "서울특별시 종로구 숭인동 766",
                        "127.001",
                        "37.570"
                ))
        );
        when(naverGeoClient.geocode(anyString())).thenReturn(geoResponse);
        when(kakaoGeoClient.geocode(anyString())).thenReturn(new KakaoGeoResponse(List.of()));

        propertyService.createProperty(request, null, List.of("https://example.com/image1.jpg"), 1L);

        ArgumentCaptor<Property> captor = ArgumentCaptor.forClass(Property.class);
        verify(propertyRepository).save(captor.capture());

        Property saved = captor.getValue();
        assertThat(saved.getLatitude()).isEqualByComparingTo(new BigDecimal("37.570"));
        assertThat(saved.getLongitude()).isEqualByComparingTo(new BigDecimal("127.001"));

        verify(propertyImageService).saveImageUrls(any(Property.class), any());
    }
}
