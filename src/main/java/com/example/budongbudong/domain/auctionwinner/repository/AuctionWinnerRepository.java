package com.example.budongbudong.domain.auctionwinner.repository;

import com.example.budongbudong.common.entity.AuctionWinner;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuctionWinnerRepository extends JpaRepository<AuctionWinner, Long> {

    boolean existsByAuctionId(Long id);

    Optional<AuctionWinner> findByAuctionId(Long auctionId);

    default AuctionWinner getAuctionWinnerOrThrow(Long  auctionId) {
        return findByAuctionId(auctionId).orElseThrow(()-> new CustomException(ErrorCode.AUCTION_WINNER_NOT_FOUND));
    }
}
