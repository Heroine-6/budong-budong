package com.example.budongbudong.domain.bid.repository;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.Bid;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long>, QBidRepository {

    @Query("""
                select b from Bid b
                where b.auction.id = :auctionId
                  and b.isHighest = true
                  and b.isDeleted = false
            """)
    Optional<Bid> findHighestBidByAuctionId(Long auctionId);

    Page<Bid> findAllByAuctionId(Long auctionId, Pageable pageable);

    Optional<Bid> findTopByAuctionOrderByPriceDescCreatedAtAsc(Auction auction);

    // 최고 입찰가 조회 (논리 삭제 제외)
    @Query("""
                select max(b.price)
                from Bid b
                where b.auction.id = :auctionId
                  and b.isDeleted = false
            """)
    Optional<Long> findHighestPriceByAuctionId(@Param("auctionId") Long auctionId);

    // 총 입찰자 수 (논리 삭제 제외)
    @Query("""
                select count(distinct b.user.id)
                from Bid b
                where b.auction.id = :auctionId
                  and b.isDeleted = false
            """)
    int countDistinctBiddersByAuctionId(@Param("auctionId") Long auctionId);

    @Query("""
                select count(distinct b.user) from Bid b
                where b.auction.id = :auctionId
            """)
    int countTotalBidders(Long auctionId);

    List<Bid> findAllByAuctionOrderByPriceDesc(Auction auction);

    default long getHighestPriceOrStartPrice(Long auctionId, long startPrice) {
        return findHighestPriceByAuctionId(auctionId)
                .orElse(startPrice);
    }

    default Bid findHighestBidOrNull(Long auctionId) {
        return findHighestBidByAuctionId(auctionId).orElse(null);
    }

    default void validateBidPriceHigherThanCurrentOrThrow(Long bidPrice, Bid highestBid) {
        if (highestBid != null && bidPrice <= highestBid.getPrice()) {
            throw new CustomException(ErrorCode.BID_PRICE_TOO_LOW);
        }
    }
}
