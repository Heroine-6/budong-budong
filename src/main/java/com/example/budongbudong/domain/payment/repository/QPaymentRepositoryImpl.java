package com.example.budongbudong.domain.payment.repository;

import com.example.budongbudong.domain.payment.dto.query.QReadAllPaymentDto;
import com.example.budongbudong.domain.payment.dto.query.QReadPaymentDetailDto;
import com.example.budongbudong.domain.payment.dto.query.ReadAllPaymentDto;
import com.example.budongbudong.domain.payment.dto.query.ReadPaymentDetailDto;
import com.example.budongbudong.domain.payment.enums.PaymentStatus;
import com.example.budongbudong.domain.payment.enums.PaymentType;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.example.budongbudong.common.entity.QPayment.payment;

@Repository
@RequiredArgsConstructor
public class QPaymentRepositoryImpl implements QPaymentRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<ReadAllPaymentDto> findAllByUserId(Long userId, Pageable pageable) {

        List<ReadAllPaymentDto> contents = queryFactory
                .select(new QReadAllPaymentDto(
                        payment.id,
                        payment.type,
                        payment.orderName,
                        payment.amount,
                        payment.status,
                        payment.approvedAt
                ))
                .from(payment)
                .where(
                        payment.user.id.eq(userId),
                        payment.isDeleted.isFalse(),
                        exposeStatus()
                )
                .orderBy(payment.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize()+1)
                .fetch();

        boolean hasNext = contents.size() > pageable.getPageSize();

        if (hasNext) {
            contents.remove(contents.size() - 1);
        }

        return new SliceImpl<>(contents, pageable, hasNext);
    }

    @Override
    public Optional<ReadPaymentDetailDto> findDetailById(Long paymentId) {

        ReadPaymentDetailDto result = queryFactory
                .select(new QReadPaymentDetailDto(
                        payment.id,
                        payment.user.id,
                        payment.status,
                        payment.type,
                        payment.amount,
                        payment.orderName,
                        payment.paymentMethodType,
                        payment.methodDetail,
                        payment.approvedAt,
                        payment.auction.id,
                        payment.auction.startPrice,
                        payment.auction.status,
                        payment.auction.startedAt,
                        payment.auction.endedAt
                ))
                .from(payment)
                .where(
                        payment.id.eq(paymentId),
                        payment.isDeleted.isFalse()
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<Long> findDepositPaymentIdsByAuctionIdAndNotWinnerUserId(Long auctionId, Long winnerUserId) {
        return queryFactory
                .select(payment.id)
                .from(payment)
                .where(
                        payment.auction.id.eq(auctionId),
                        payment.type.eq(PaymentType.DEPOSIT),
                        payment.status.eq(PaymentStatus.SUCCESS),
                        payment.user.id.ne(winnerUserId)
                )
                .fetch();
    }

    private BooleanExpression exposeStatus() {
        return payment.status.in(
                PaymentStatus.VERIFYING,
                PaymentStatus.SUCCESS,
                PaymentStatus.FAIL
        );
    }
}
