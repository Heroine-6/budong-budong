package com.example.budongbudong.domain.payment.repository;

import com.example.budongbudong.common.entity.Payment;
import com.example.budongbudong.domain.payment.enums.PaymentStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.budongbudong.common.entity.QPayment.payment;

@Repository
@RequiredArgsConstructor
public class QPaymentRepositoryImpl implements QPaymentRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<Payment> findAllByUserId(Long userId, Pageable pageable) {

        List<Payment> contents = queryFactory
                .selectFrom(payment)
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

    private BooleanExpression exposeStatus() {
        return payment.status.in(
                PaymentStatus.VERIFYING,
                PaymentStatus.SUCCESS,
                PaymentStatus.FAIL
        );
    }
}
