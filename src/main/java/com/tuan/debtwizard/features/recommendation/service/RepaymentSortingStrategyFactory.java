package com.tuan.debtwizard.features.recommendation.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.recommendation.model.RepaymentStrategy;
import org.springframework.stereotype.Service;

@Service
public class RepaymentSortingStrategyFactory {

    private final ApplySnowball snowball;
    private final ApplyAvalanche avalanche;

    public RepaymentSortingStrategyFactory(
            ApplySnowball snowball,
            ApplyAvalanche avalanche
    ) {
        this.snowball = snowball;
        this.avalanche = avalanche;
    }

    public RepaymentSortingStrategy get(RepaymentStrategy strategy) {
        if (strategy == null) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_STRATEGY);
        }
        return switch (strategy) {
            case SNOWBALL -> snowball;
            case AVALANCHE -> avalanche;
        };
    }
}