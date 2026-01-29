package com.example.budongbudong.common.entity;

import com.example.budongbudong.domain.property.enums.PropertyType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 실거래가 엔티티
 *
 * 국토교통부 공공데이터에서 수집한 아파트/오피스텔/빌라 실거래 정보
 * - 지오코딩 전: latitude/longitude = null
 * - 지오코딩 후: 좌표 + 도로명 주소 저장
 * - 지오코딩 실패: (0, 0) 저장하여 재처리 방지
 */
@Getter
@Entity
@Table(name = "real_deals",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_real_deal_dedup",
                columnNames = {"property_name", "address", "deal_amount", "deal_date", "floor"}
        ),
        indexes = {
                @Index(name = "idx_rd_lat_null", columnList = "latitude"),  // 지오코딩 미완료 조회용
                @Index(name = "idx_rd_address", columnList = "address")
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RealDeal extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_name", length = 100, nullable = false)
    private String propertyName;

    @Column(name = "address", length = 255, nullable = false)
    private String address;

    @Column(name = "deal_amount", nullable = false)
    private BigDecimal dealAmount;

    @Column(name = "exclusive_area", precision = 10, scale = 2)
    private BigDecimal exclusiveArea;

    @Column(name = "floor")
    private Integer floor;

    @Column(name = "built_year")
    private Integer builtYear;

    @Column(name = "deal_date", nullable = false)
    private LocalDate dealDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", length = 20, nullable = false)
    private PropertyType propertyType;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "road_address", length = 255)
    private String roadAddress;

    @Column(name = "lawd_cd", length = 5)
    private String lawdCd;

    @Builder
    public RealDeal(String propertyName, String address, BigDecimal dealAmount,
                    BigDecimal exclusiveArea, Integer floor, Integer builtYear,
                    LocalDate dealDate, PropertyType propertyType, String lawdCd) {
        this.propertyName = propertyName;
        this.address = address;
        this.dealAmount = dealAmount;
        this.exclusiveArea = exclusiveArea;
        this.floor = floor;
        this.builtYear = builtYear;
        this.dealDate = dealDate;
        this.propertyType = propertyType;
        this.lawdCd = lawdCd;
    }

    public void applyGeoCode(BigDecimal latitude, BigDecimal longitude, String roadAddress) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.roadAddress = roadAddress;
    }

    public boolean isGeoCoded() {
        return this.latitude != null && this.longitude != null;
    }
}
