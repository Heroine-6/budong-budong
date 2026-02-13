package com.example.budongbudong.domain.auction.repository;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.auction.enums.AuctionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    Optional<Auction> findByPropertyId(Long propertyId);

    boolean existsByPropertyIdAndStatusNotIn(Long propertyId, Iterable<AuctionStatus> statuses);

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

    default Auction findByPropertyIdOrThrowIfExists(Long propertyId) {
        return findByPropertyId(propertyId).orElse(null);
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

    default Auction getAuctionWithPropertyOrTrow(Long auctionId) {
        return findByIdWithProperty(auctionId).orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));
    }

    @Query("""
                select a
                from Auction a
                where a.property.id in :propertyIds
            """)
    List<Auction> findAllByPropertyIds(@Param("propertyIds") List<Long> propertyIds);

    @Query("""
                select a.endedAt
                from Auction a
                where a.id = :auctionId
            """)
    Optional<LocalDateTime> findEndedAtById(@Param("auctionId") Long auctionId);

    default LocalDateTime getEndedAtOrThrow(Long auctionId) {
        return findEndedAtById(auctionId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));
    }

    @Query("""
                select a.startedAt
                from Auction a
                where a.id = :auctionId
            """)
    Optional<LocalDateTime> findStartedAtById(@Param("auctionId") Long auctionId);

    default LocalDateTime getStartedAtOrThrow(Long auctionId) {
        return findStartedAtById(auctionId)
                .orElseThrow(() -> new CustomException(ErrorCode.AUCTION_NOT_FOUND));
    }

    @Query("""
                select a.id
                from Auction a
                where a.status = 'SCHEDULED'
                  and a.startedAt <= :today
            """)
    List<Long> findOpenAuctionIds(LocalDateTime today);

    @Modifying
    @Query("""
                update Auction a
                set a.status = 'OPEN'
                where a.id in :ids
                  and a.status = 'SCHEDULED'
            """)
    int openScheduled(List<Long> ids);

    @Query("""
                select a.id
                from Auction a
                where a.status = 'OPEN'
                  and a.endedAt < :today
            """)
    List<Long> findEndedAuctionIds(LocalDateTime today);

    @Modifying
    @Query("""
                update Auction a
                set a.status = 'CLOSED'
                where a.id in :ids
                  and a.status = 'OPEN'
            """)
    int closeOpened(List<Long> ids);

    @Query("""
                select a
                from Auction a
                join fetch a.property p
                where a.id = :auctionId
            """)
    Optional<Auction> findByIdWithProperty(Long auctionId);

    @Query("""
                select a.id
                from Auction a
                where a.status = 'OPEN'
                  and a.endedAt = :today
            """)
    List<Long> findEndingSoonAuctionIds(LocalDateTime today);

    List<Auction> findAllByStatusAndType(AuctionStatus status, AuctionType type);

}
