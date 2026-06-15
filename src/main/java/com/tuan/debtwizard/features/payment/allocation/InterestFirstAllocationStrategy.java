package com.tuan.debtwizard.features.payment.allocation;


import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.payment.dto.PaymentAllocationResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class InterestFirstAllocationStrategy implements PaymentAllocationStrategy {


    @Override
    public PaymentAllocationResult allocate( BigDecimal remainingPrincipal, BigDecimal remainingInterest, BigDecimal amount) {
        remainingInterest = validate(remainingInterest);
        remainingPrincipal = validate(remainingPrincipal);
        BigDecimal remaining = amount;

        // Trả lãi trước
        BigDecimal interestPaid = remainingInterest.min(remaining);
        remainingInterest = remainingInterest.subtract(interestPaid);
        remaining = remaining.subtract(interestPaid);

        // Sau đó trả gốc
        BigDecimal principalPaid = remainingPrincipal.min(remaining);
        remainingPrincipal = remainingPrincipal.subtract(principalPaid);
        remaining = remaining.subtract(principalPaid);

        return new PaymentAllocationResult(principalPaid, interestPaid,
                remainingPrincipal,      // remainingPrincipal
                remainingInterest,       // remainingInterest
                remaining);
    }

    private BigDecimal validate(BigDecimal number) {
        if (number == null) {
            return BigDecimal.ZERO;
        };
        return number;
    }
}