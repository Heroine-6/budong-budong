package com.example.budongbudong.domain.property.realdeal.repository;

import com.example.budongbudong.common.entity.RealDeal;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RealDealRepository extends JpaRepository<RealDeal, Long> {

    List<RealDeal> findByLatitudeIsNull(Pageable pageable);

    long countByLatitudeIsNull();

    long countByRoadAddressIsNotNull();

    @Query("""
            SELECT rd FROM RealDeal rd
            WHERE rd.id > :lastId
              AND rd.latitude IS NOT NULL
              AND rd.latitude <> 0
              AND rd.isDeleted = false
            ORDER BY rd.id ASC
            """)
    List<RealDeal> findGeoCodedAfter(@Param("lastId") Long lastId, Pageable pageable);
}
