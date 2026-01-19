package com.example.budongbudong.domain.auction.service;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.AuctionWinner;
import com.example.budongbudong.common.entity.Bid;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.auctionwinner.repository.AuctionWinnerRepository;
import com.example.budongbudong.domain.bid.enums.BidStatus;
import com.example.budongbudong.domain.bid.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionSchedulerService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final AuctionWinnerRepository auctionWinnerRepository;

    /**
     * 시작 시간이 된 경매 OPEN 처리
     */
    @Transactional
    public void openScheduledAction() {

        LocalDateTime now = LocalDateTime.now();

        List<Auction> auctions = auctionRepository.findByStatusAndStartedAtLessThanEqual(
                AuctionStatus.SCHEDULED, now
        );
        for (Auction auction : auctions) {
            auction.updateStatus(AuctionStatus.OPEN);
        }
    }

    /**
     * 종료 시간이 된 경매 CLOSED + 낙찰/유찰 처리
     */
    @Transactional
    public void closeEndedAction() {

        LocalDateTime now = LocalDateTime.now();

        List<Auction> auctions = auctionRepository.findByStatusAndEndedAtLessThanEqual(
                AuctionStatus.OPEN, now
        );
        for (Auction auction : auctions) {

            int updatedAuctionNum = auctionRepository.closeIfOpen(auction.getId());
            log.info("auctionId={}, closeIfOpen updated={}", auction.getId(), updatedAuctionNum);

            if (updatedAuctionNum == 0) {
                continue;
            }

            if (auctionWinnerRepository.existsByAuctionId(auction.getId())) {
                log.warn("auctionId={} already has winner. skip", auction.getId());
                continue;
            }

            Optional<Bid> highestBid = bidRepository.findTopByAuctionOrderByPriceDescCreatedAtAsc(auction);

            if (highestBid.isPresent()) {
                Bid bid = highestBid.get();

                auctionWinnerRepository.save(
                        AuctionWinner.create(
                                auction,
                                bid.getUser(),
                                bid.getPrice()
                        )
                );
                bid.changeStatus(BidStatus.WON);
            }
        }
    }
}
