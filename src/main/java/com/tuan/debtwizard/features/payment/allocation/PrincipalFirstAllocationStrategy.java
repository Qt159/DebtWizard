package com.tuan.debtwizard.features.payment.allocation;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.payment.dto.PaymentAllocationResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PrincipalFirstAllocationStrategy implements PaymentAllocationStrategy {
    @Override
    public PaymentAllocationResult allocate(Debt debt, BigDecimal amount) {

        BigDecimal interest = validate(debt.getAccruedInterest());
        BigDecimal principal = validate(debt.getRemainingPrincipal());

        BigDecimal remaining = amount;

        BigDecimal principalPaid = principal.min(remaining);
        principal = principal.subtract(principalPaid);
        remaining = remaining.subtract(principalPaid);

        BigDecimal interestPaid = interest.min(remaining);
        interest = interest.subtract(interestPaid);
        remaining = remaining.subtract(interestPaid);

        debt.setAccruedInterest(interest);
        debt.setRemainingPrincipal(principal);

        return new PaymentAllocationResult(principalPaid, interestPaid, remaining);
    }
    private BigDecimal validate(BigDecimal number) {
        if (number == null) {
            return BigDecimal.ZERO;
        };
        return number;
    }

}