package com.example.budongbudong.domain.auction.repository;

import com.example.budongbudong.domain.auction.entity.Auction;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    Optional<Auction> findByPropertyId(Long propertyId);

    boolean existsByPropertyIdAndStatusNot(Long propertyId, AuctionStatus auctionStatus);
}
