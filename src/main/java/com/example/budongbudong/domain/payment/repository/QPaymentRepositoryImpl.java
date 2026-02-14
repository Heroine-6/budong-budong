package com.example.budongbudong.domain.payment.repository;

import com.example.budongbudong.common.entity.QPayment;
import com.example.budongbudong.domain.auction.enums.AuctionStatus;
import com.example.budongbudong.domain.payment.dto.query.*;
import com.example.budongbudong.domain.payment.enums.PaymentStatus;
import com.example.budongbudong.domain.payment.enums.PaymentType;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.example.budongbudong.common.entity.QAuction.auction;
import static com.example.budongbudong.common.entity.QAuctionWinner.auctionWinner;
import static com.example.budongbudong.common.entity.QPayment.payment;
import static com.example.budongbudong.common.entity.QProperty.property;

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

    @Override
    public List<RequiredPaymentDto> findRequiredPaymentsByUserId(Long userId, PaymentType type) {

        QPayment sumSub = new QPayment("sumSub");
        QPayment downSub = new QPayment("downSub");

        return queryFactory
                .select(new QRequiredPaymentDto(
                        auction.id,
                        property.name,
                        auctionWinner.price,
                        // 이미 낸 금액 (보증금+계약금)
                        JPAExpressions
                                .select(sumSub.amount.sumBigDecimal().coalesce(BigDecimal.ZERO))
                                .from(sumSub)
                                .where(
                                        sumSub.auction.id.eq(auction.id),
                                        sumSub.status.eq(PaymentStatus.SUCCESS),
                                        sumSub.type.in(
                                                PaymentType.DEPOSIT,
                                                PaymentType.DOWN_PAYMENT
                                        )
                                ),
                        auctionWinner.createdAt,
                        // 계약금 승인 시점
                        JPAExpressions
                                .select(downSub.approvedAt)
                                .from(downSub)
                                .where(
                                        downSub.auction.id.eq(auction.id),
                                        downSub.type.eq(PaymentType.DOWN_PAYMENT),
                                        downSub.status.eq(PaymentStatus.SUCCESS)
                                )
                ))
                .from(auctionWinner)
                .join(auctionWinner.auction, auction)
                .join(auction.property, property)
                .where(requiredPaymentCondition(userId, type))
                .fetch();
    }



    private BooleanExpression exposeStatus() {
        return payment.status.in(
                PaymentStatus.VERIFYING,
                PaymentStatus.SUCCESS,
                PaymentStatus.FAIL,
                PaymentStatus.REFUND_REQUESTED,
                PaymentStatus.REFUNDED
        );
    }

    /**
     * 해당 유저의 낙찰 건이며, 경매가 종료된 경우만 대상
     * 현재 결제 타입(type)에 대해 SUCCESS가 없을 때만 노출
     * BALANCE는 계약금(DOWN_PAYMENT) SUCCESS 이후에만 노출
     */
    private BooleanExpression requiredPaymentCondition(Long userId, PaymentType type) {

        QPayment paySub = new QPayment("paySub");

        BooleanExpression condition =
                auctionWinner.user.id.eq(userId)
                        .and(auction.status.eq(AuctionStatus.CLOSED))
                        .and(
                                JPAExpressions
                                        .selectOne()
                                        .from(paySub)
                                        .where(
                                                paySub.auction.id.eq(auction.id),
                                                paySub.user.id.eq(userId),
                                                paySub.type.eq(type),
                                                paySub.status.eq(PaymentStatus.SUCCESS)
                                        )
                                        .notExists()
                        );

        if (type == PaymentType.BALANCE) {
            condition = condition.and(
                    JPAExpressions
                            .selectOne()
                            .from(paySub)
                            .where(
                                    paySub.auction.id.eq(auction.id),
                                    paySub.user.id.eq(userId),
                                    paySub.type.eq(PaymentType.DOWN_PAYMENT),
                                    paySub.status.eq(PaymentStatus.SUCCESS)
                            )
                            .exists()
            );
        }

        return condition;
    }

}
