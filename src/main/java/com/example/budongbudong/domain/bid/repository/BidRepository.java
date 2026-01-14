package com.example.budongbudong.domain.bid.repository;

import com.example.budongbudong.domain.bid.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidRepository extends JpaRepository<Bid, Long> {
}
