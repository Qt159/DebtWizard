package com.tuan.debtwizard.features.payment.allocation;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.payment.model.PaymentAllocationRule;
import org.springframework.stereotype.Service;

@Service
public class PaymentAllocationStrategyFactory {

    private final InterestFirstAllocationStrategy interestFirst;
    private final PrincipalFirstAllocationStrategy principalFirst;

    public PaymentAllocationStrategyFactory(
            InterestFirstAllocationStrategy interestFirst,
            PrincipalFirstAllocationStrategy principalFirst
    ) {
        this.interestFirst = interestFirst;
        this.principalFirst = principalFirst;
    }

    public PaymentAllocationStrategy get(PaymentAllocationRule rule) {
        if (rule == null) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_RULE);}
        return switch (rule) {
            case INTEREST_FIRST -> interestFirst;
            case PRINCIPAL_FIRST -> principalFirst;
        };
    }
}