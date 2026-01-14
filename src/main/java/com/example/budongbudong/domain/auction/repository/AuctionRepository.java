package com.example.budongbudong.domain.auction.repository;

import com.example.budongbudong.domain.auction.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
}
