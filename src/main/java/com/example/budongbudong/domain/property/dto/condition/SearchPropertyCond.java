package com.example.budongbudong.domain.property.dto.condition;

import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.property.enums.PropertyType;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.Year;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class SearchPropertyCond {
    private AuctionStatus status;
    private String name;
    private PropertyType type;
    private String address;
    private Long minPrice;
    private Long maxPrice;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate migrateDate;
    private Year builtYear;
}
