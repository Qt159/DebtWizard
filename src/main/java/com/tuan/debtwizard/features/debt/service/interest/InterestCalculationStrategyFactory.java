package com.tuan.debtwizard.features.debt.service.interest;
import com.tuan.debtwizard.features.debt.model.InterestCalculationMethod;
import com.tuan.debtwizard.features.debt.service.interest.strategy.FlatInterestCalculationStrategy;
import com.tuan.debtwizard.features.debt.service.interest.strategy.ReducingBalanceInterestCalculationStrategy;
import org.springframework.stereotype.Service;

@Service
public class InterestCalculationStrategyFactory {
    private final FlatInterestCalculationStrategy flat;
    private final ReducingBalanceInterestCalculationStrategy reducing;

    public InterestCalculationStrategyFactory(FlatInterestCalculationStrategy flat,
                                              ReducingBalanceInterestCalculationStrategy reducing) {
        this.flat = flat;
        this.reducing = reducing;
    }

    public InterestCalculationStrategy get(InterestCalculationMethod method) {
        return switch (method) {
            case FLAT -> flat;
            case REDUCING_BALANCE -> reducing;
        };
    }
}