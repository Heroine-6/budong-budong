package com.example.budongbudong.domain.auction.repository;

import com.example.budongbudong.domain.auction.entity.Auction;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    Optional<Auction> findByPropertyId(Long propertyId);

    boolean existsByPropertyIdAndStatusNotIn(Long propertyId, Iterable<AuctionStatus> statuses);

    List<Auction> findByStatusAndStartedAtLessThanEqual(
            AuctionStatus status,
            LocalDateTime time
    );

    List<Auction> findByStatusAndEndedAtLessThanEqual(
            AuctionStatus status,
            LocalDateTime time
    );

    @Modifying
    @Query("""
        update Auction a
        set a.status = 'CLOSED'
        where a.id = :auctionId
        and a.status = 'OPEN'
    """)
    int closeIfOpen(@Param("auctionId") Long auctionId);
}
