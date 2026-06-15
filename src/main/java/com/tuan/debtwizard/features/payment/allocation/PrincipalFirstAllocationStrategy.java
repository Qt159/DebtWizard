package com.tuan.debtwizard.features.payment.allocation;

import com.tuan.debtwizard.features.payment.dto.PaymentAllocationResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PrincipalFirstAllocationStrategy implements PaymentAllocationStrategy {

    @Override
    public PaymentAllocationResult allocate(
            BigDecimal principal,
            BigDecimal interest,
            BigDecimal amount
    ) {

        principal = validate(principal);
        interest = validate(interest);

        BigDecimal remaining = amount;

        // Trả gốc trước
        BigDecimal principalPaid = principal.min(remaining);
        principal = principal.subtract(principalPaid);
        remaining = remaining.subtract(principalPaid);

        // Sau đó trả lãi
        BigDecimal interestPaid = interest.min(remaining);
        interest = interest.subtract(interestPaid);
        remaining = remaining.subtract(interestPaid);

        return new PaymentAllocationResult(
                principalPaid,
                interestPaid,
                principal,      // remainingPrincipal
                interest,       // remainingInterest
                remaining
        );
    }

    private BigDecimal validate(BigDecimal number) {
        if (number == null) {
            return BigDecimal.ZERO;
        };
        return number;
    }
}