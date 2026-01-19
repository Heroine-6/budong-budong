package com.example.budongbudong.domain.bid.repository;

import com.example.budongbudong.domain.bid.dto.response.ReadMyBidsResponse;
import com.example.budongbudong.domain.bid.enums.BidStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.budongbudong.common.entity.QAuction.auction;
import static com.example.budongbudong.common.entity.QBid.bid;
import static com.example.budongbudong.common.entity.QProperty.property;

@Repository
@RequiredArgsConstructor
public class QBidRepositoryImpl implements QBidRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 내 입찰 내역 페이징 조회
     */
    @Override
    public Page<ReadMyBidsResponse> findMyBids(Long userId, String status, Pageable pageable) {

        List<ReadMyBidsResponse> results = queryFactory
                .select(Projections.constructor(ReadMyBidsResponse.class,
                        property.id,
                        bid.status,
                        property.name,
                        bid.price,
                        auction.endedAt
                ))
                .from(bid)
                .leftJoin(auction).on(auction.id.eq(bid.auction.id))
                .leftJoin(property).on(property.id.eq(auction.property.id))
                .where(
                        bid.user.id.eq(userId),
                        eqStatus(status)
                )
                .orderBy(property.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total =
                queryFactory
                        .select(bid.count())
                        .from(bid)
                        .where(
                                bid.user.id.eq(userId),
                                eqStatus(status)
                        )
                        .fetchFirst();

        return new PageImpl<>(results, pageable, total);
    }

    private BooleanExpression eqStatus(String status) {
        return (status != null) ? bid.status.eq(BidStatus.valueOf(status)) : null;
    }
}
