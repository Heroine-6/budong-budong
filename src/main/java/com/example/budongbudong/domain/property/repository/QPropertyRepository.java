package com.example.budongbudong.domain.property.repository;

import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.property.dto.response.ReadAllPropertyResponse;
import com.example.budongbudong.domain.property.enums.PropertyType;
import org.springframework.data.domain.*;

public interface QPropertyRepository {

    Page<ReadAllPropertyResponse> findAllMyProperties(Long userId, Pageable pageable);

    Slice<ReadAllPropertyResponse> findPropertyList(PropertyType type, AuctionStatus auctionStatus, Pageable pageable);
}
