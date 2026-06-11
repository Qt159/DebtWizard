package com.tuan.debtwizard.features.interest.mapper;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.interest.dto.InterestConfigRequest;
import com.tuan.debtwizard.features.interest.model.InterestConfig;
import org.springframework.stereotype.Component;

@Component
public class InterestConfigMapper {
    public InterestConfig toEntity(InterestConfigRequest request, Debt debt) {
        InterestConfig ic = new InterestConfig();
        ic.setDebt(debt);
        ic.setInterestRate(request.getInterestRate());
        ic.setInterestCalculationMethod(request.getInterestCalculationMethod());
        ic.setInterestRatePeriod(request.getInterestRatePeriod());
        ic.setPaymentAllocationRule(request.getPaymentAllocationRule());
        ic.setGracePeriodDays(request.getGracePeriodDays());
        ic.setLateFee(request.getLateFee());
        return ic;
    }
}