package com.example.budongbudong.domain.property.entity;

import com.example.budongbudong.common.entity.BaseEntity;
import com.example.budongbudong.domain.auction.entity.Auction;
import com.example.budongbudong.domain.propertyimage.entity.PropertyImage;
import com.example.budongbudong.domain.property.enums.PropertyType;
import com.example.budongbudong.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "properties")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Property extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "address", length = 255, nullable = false)
    private String address;

    @Column(name = "floor", nullable = false)
    private int floor;

    @Column(name = "total_floor", nullable = false)
    private int totalFloor;

    @Column(name = "room_count", nullable = false)
    private int roomCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    private PropertyType type;

    @Column(name = "built_year",nullable = false)
    private Year builtYear;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "migrate_date", nullable = false)
    private LocalDate migrateDate;

    @Column(name = "supply_area", precision = 10, scale=2 ,nullable = false)
    private BigDecimal supplyArea;

    @Column(name = "private_area", precision = 10, scale=2 ,nullable = false)
    private BigDecimal privateArea;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL)
    private List<PropertyImage> propertyImageList = new ArrayList<>();

    @OneToOne(mappedBy = "property", fetch = FetchType.LAZY)
    private Auction auction;

    @Builder
    public Property(String name, String address, int floor, int totalFloor, int roomCount,
                    PropertyType type, Year builtYear, String description, Long price,
                    LocalDate migrateDate, BigDecimal supplyArea, BigDecimal privateArea,
                    User user) {
        this.name = name;
        this.address = address;
        this.floor = floor;
        this.totalFloor = totalFloor;
        this.roomCount = roomCount;
        this.type = type;
        this.builtYear = builtYear;
        this.description = description;
        this.price = price;
        this.migrateDate = migrateDate;
        this.supplyArea = supplyArea;
        this.privateArea = privateArea;
        this.user = user;
    }

    public void addImage(PropertyImage image) {
        this.propertyImageList.add(image);
    }

    public void update(Long price, LocalDate migrateDate, String description) {
        if (price != null) {
            this.price = price;
        }

        if (migrateDate != null) {
            this.migrateDate = migrateDate;
        }

        if (description != null) {
            this.description = description;
        }
    }
}
