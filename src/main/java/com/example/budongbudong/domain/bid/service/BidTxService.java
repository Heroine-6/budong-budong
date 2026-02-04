package com.example.budongbudong.domain.bid.service;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.Bid;
import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.domain.auction.repository.AuctionRepository;
import com.example.budongbudong.domain.bid.dto.request.CreateBidRequest;
import com.example.budongbudong.domain.bid.dto.response.CreateBidResponse;
import com.example.budongbudong.domain.bid.enums.BidStatus;
import com.example.budongbudong.domain.bid.repository.BidRepository;
import com.example.budongbudong.domain.notification.enums.NotificationType;
import com.example.budongbudong.domain.notification.event.BidCreatedEvent;
import com.example.budongbudong.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidTxService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 입찰 등록 - Create
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CreateBidResponse createBidTx(CreateBidRequest request, Long auctionId, Long userId) {

        String th = Thread.currentThread().getName();

        User user = userRepository.getByIdOrThrow(userId);

        Auction auction = auctionRepository.getOpenAuctionOrThrow(auctionId);

        BigDecimal bidPrice = request.getPrice();
        BigDecimal currentMaxPrice = bidRepository.findMaxPriceByAuctionId(auctionId);

        BigDecimal minBidIncrement = auction.getMinBidIncrement();

        // 첫 입찰은 시작가 이상, 이후 입찰은 최고가 + 최소 입찰 단위 이상.
        BigDecimal minimumRequired = currentMaxPrice == null
                ? auction.getStartPrice()
                : currentMaxPrice.add(minBidIncrement);

        if (bidPrice.compareTo(minimumRequired) < 0) {
            log.info("[{}] 최소입찰미달 auctionId={} bid={} minimum={}", th, auctionId, bidPrice, minimumRequired);
            return CreateBidResponse.rejectedFrom(BidStatus.REJECTED, "입찰가는 현재 최고가보다 높아야 합니다.");
        }

        if ((bidPrice.subtract(minimumRequired)).remainder(minBidIncrement).compareTo(BigDecimal.ZERO) != 0) {
            log.info("[{}] 입찰단위오류 auctionId={} bid={} minimum={} increment={}", th, auctionId, bidPrice, minimumRequired, minBidIncrement);
            return CreateBidResponse.rejectedFrom(BidStatus.REJECTED, "입찰 금액이 올바르지 않습니다.");
        }

        bidRepository.unmarkHighestAndOutbidByAuctionId(auctionId);

        Bid bid = new Bid(user, auction, bidPrice);
        bid.markHighest();
        bid.changeStatus(BidStatus.WINNING);

        Bid savedBid = bidRepository.save(bid);

        log.info("[{}] SUCCESS auctionId={} price={}", th, auctionId, bidPrice);

        eventPublisher.publishEvent(new BidCreatedEvent(auctionId, userId, NotificationType.BID_UPDATE));

        return CreateBidResponse.from(savedBid);
    }
}
