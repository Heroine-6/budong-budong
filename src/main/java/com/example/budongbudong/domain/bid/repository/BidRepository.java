package com.example.budongbudong.domain.bid.repository;

import com.example.budongbudong.domain.auction.entity.Auction;
import com.example.budongbudong.domain.bid.entity.Bid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {

    @Query("""
                select b from Bid b
                where b.auction.id = :auctionId
                  and b.isHighest = true
                  and b.isDeleted = false
            """)
    Optional<Bid> findHighestBidByAuctionId(Long auctionId);

    @Query(
            value = """
                        select b
                        from Bid b
                        join fetch b.auction a
                        join fetch a.property p
                        where b.user.id = :userId
                    """,
            countQuery = """
                        select count(b)
                        from Bid b
                        where b.user.id = :userId
                    """
    )
    Page<Bid> findMyBidsPage(@Param("userId") Long userId, Pageable pageable);

    Page<Bid> findAllByAuctionId(Long auctionId, Pageable pageable);

    Optional<Bid> findTopByAuctionOrderByPriceDescCreatedAtAsc(Auction auction);

    @Query("""
                select count(distinct b.user) from Bid b
                where b.auction.id = :auctionId
            """)
    int countTotalBidders(Long auctionId);

    List<Bid> findAllByAuctionOrderByPriceDesc(Auction auction);
}
