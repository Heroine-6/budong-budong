package com.example.budongbudong.domain.bid.repository;

import com.example.budongbudong.domain.bid.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface BidRepository extends JpaRepository<Bid, Long> {


    @Query("""
        select b from Bid b
        where b.auction.id = :auctionId
          and b.isHighest = true
          and b.isDeleted = false
    """)
    Optional<Bid> findHighestBidByAuctionId(Long auctionId);

    List<Bid> findAllByAuctionIdOrderByCreatedAtDesc(Long auctionId);
}
