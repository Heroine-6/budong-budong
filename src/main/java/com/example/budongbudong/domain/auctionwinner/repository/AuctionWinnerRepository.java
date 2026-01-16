package com.example.budongbudong.domain.auctionwinner.repository;

import com.example.budongbudong.domain.auctionwinner.entity.AuctionWinner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionWinnerRepository extends JpaRepository<AuctionWinner, Long> {

    boolean existsByAuctionId(Long id);
}
