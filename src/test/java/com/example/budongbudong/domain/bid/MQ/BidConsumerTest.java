package com.example.budongbudong.domain.bid.MQ;

import com.example.budongbudong.common.entity.Auction;
import com.example.budongbudong.common.entity.Bid;
import com.example.budongbudong.common.entity.User;
import com.example.budongbudong.common.exception.CustomException;
import com.example.budongbudong.common.exception.ErrorCode;
import com.example.budongbudong.domain.bid.dto.request.CreateBidMessage;
import com.example.budongbudong.domain.bid.dto.request.CreateBidRequest;
import com.example.budongbudong.domain.bid.dto.response.CreateBidResponse;
import com.example.budongbudong.domain.bid.service.BidService;
import com.example.budongbudong.fixture.AuctionFixture;
import com.example.budongbudong.fixture.BidFixture;
import com.example.budongbudong.fixture.PropertyFixture;
import com.example.budongbudong.fixture.UserFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BidConsumer 입찰 메시지 처리 테스트")
class BidConsumerTest {

    @InjectMocks
    private BidConsumer bidConsumer;

    @Mock
    private BidService bidService;

    @Mock
    private BidPublisher bidPublisher;

    private CreateBidMessage message;
    private Bid bid;

    @BeforeEach
    void setUp() {

        CreateBidRequest bidRequest = new CreateBidRequest();
        ReflectionTestUtils.setField(bidRequest, "price", BigDecimal.valueOf(500_000_000));

        message = CreateBidMessage.from(1L, 1L, bidRequest);

        User seller = UserFixture.sellerUser();
        User bidder = UserFixture.generalUser();
        Auction auction = AuctionFixture.openEndedAuction(
                PropertyFixture.property(seller),
                LocalDateTime.now()
        );
        bid = BidFixture.bid(bidder, auction);
    }

    @Test
    @DisplayName("입찰 메시지를 정상 처리한다")
    void success() {

        // given
        CreateBidResponse response = CreateBidResponse.from(bid);
        when(bidService.createBid(any(), eq(1L), eq(1L))).thenReturn(response);

        // when
        bidConsumer.receiveCreateBidMessage(message);

        // then
        verify(bidService).createBid(any(), eq(1L), eq(1L));
        verify(bidPublisher, never()).publishRetry(any());
    }

    @Test
    @DisplayName("비즈니스 예외 시 재시도하지 않고 ACK 처리한다")
    void business_exception_no_retry() {

        // given
        when(bidService.createBid(any(), eq(1L), eq(1L)))
                .thenThrow(new CustomException(ErrorCode.AUCTION_NOT_OPEN));

        // when
        bidConsumer.receiveCreateBidMessage(message);

        // then
        verify(bidPublisher, never()).publishRetry(any());
        assertThat(message.getRetryCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("BID_LOCK_TIMEOUT 시 지연 큐에 재시도 메시지를 발행한다")
    void lock_timeout_retry() {

        // given
        when(bidService.createBid(any(), eq(1L), eq(1L)))
                .thenThrow(new CustomException(ErrorCode.BID_LOCK_TIMEOUT));

        // when
        bidConsumer.receiveCreateBidMessage(message);

        // then
        verify(bidPublisher).publishRetry(message);
    }

    @Test
    @DisplayName("BID_LOCK_FAILED 시 재시도하지 않고 ACK 처리한다")
    void lock_failed_no_retry() {

        // given
        when(bidService.createBid(any(), eq(1L), eq(1L)))
                .thenThrow(new CustomException(ErrorCode.BID_LOCK_FAILED));

        // when
        bidConsumer.receiveCreateBidMessage(message);

        // then
        verify(bidPublisher, never()).publishRetry(any());
    }

    @Test
    @DisplayName("인프라 오류 시 지연 큐에 재시도 메시지를 발행한다")
    void infra_exception_retry() {

        // given
        when(bidService.createBid(any(), eq(1L), eq(1L)))
                .thenThrow(new RuntimeException("DB connection failed"));

        // when
        bidConsumer.receiveCreateBidMessage(message);

        // then
        verify(bidPublisher).publishRetry(message);
    }
}
