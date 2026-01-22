package com.example.budongbudong.domain.auction.repository;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

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

    default Auction getByIdOrThrow(Long auctionId) {
        return findById(auctionId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));
    }

    default void getByPropertyIdOrThrowIfExists(Long propertyId) {
        Auction auction = findByPropertyId(propertyId).orElse(null);

        if (auction != null && auction.getStatus() != AuctionStatus.CANCELLED) {
            throw new CustomException(ErrorCode.AUCTION_ALREADY_EXISTS);
        }
    }

    default Auction getOpenAuctionOrThrow(Long auctionId) {
        Auction auction = findById(auctionId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));

        if (auction.getStatus() != AuctionStatus.OPEN) {
            throw new CustomException(ErrorCode.AUCTION_NOT_OPEN);
        }

        return auction;
    }


    default void validateExistsOrThrow(Long auctionId) {
        if (!existsById(auctionId)) {
            throw new CustomException(ErrorCode.AUCTION_NOT_FOUND);
        }
    }

    default Auction findByPropertyIdOrNull(Long propertyId) {
        return findByPropertyId(propertyId).orElse(null);
    }

    default AuctionStatus findStatusByPropertyIdOrNull(Long propertyId) {
        Auction auction = findByPropertyIdOrNull(propertyId);
        return (auction == null) ? null : auction.getStatus();
    }

    default void validatePropertyDeletableOrThrow(Long propertyId) {
        boolean hasNonDeletableAuction =
                existsByPropertyIdAndStatusNotIn(
                        propertyId,
                        List.of(AuctionStatus.SCHEDULED, AuctionStatus.CANCELLED)
                );

        if (hasNonDeletableAuction) {
            throw new CustomException(ErrorCode.PROPERTY_CANNOT_DELETE);
        }
    }

    @Query("""
            select a from Auction a
            where a.property.id in :propertyIds
        """)
    List<Auction> findAllByPropertyIds(@Param("propertyIds") List<Long> propertyIds);

}
