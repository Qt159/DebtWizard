package com.tuan.debtwizard.features.payment.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.payment.allocation.PaymentAllocationStrategyFactory;
import com.tuan.debtwizard.features.payment.dto.PaymentAllocationResult;
import com.tuan.debtwizard.features.payment.model.PaymentAllocationRule;
import com.tuan.debtwizard.features.payment.allocation.PaymentAllocationStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
@Service
public class PaymentAllocationService {

    private final PaymentAllocationStrategyFactory factory;

    public PaymentAllocationService(PaymentAllocationStrategyFactory factory) {
        this.factory = factory;
    }
    public PaymentAllocationResult allocate(Debt debt, BigDecimal amount) {
        validate(debt, amount);

        PaymentAllocationRule rule =
                debt.getInterestConfig().getPaymentAllocationRule();
        if (rule == null) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_RULE);
        }
        PaymentAllocationStrategy strategy = factory.get(rule);
        PaymentAllocationResult result = strategy.allocate(debt.getRemainingPrincipal(), debt.getAccruedInterest(), amount);

        // Cập nhật lại trong db
        debt.setRemainingPrincipal(result.getRemainingPrincipal());
        debt.setAccruedInterest(result.getRemainingInterest());

        return result;
    }


    private void validate(Debt debt, BigDecimal amount) {
        if (debt == null) {
            throw new AppException(ErrorCode.DEBT_NOT_FOUND);}

        if (debt.getStatus() != DebtStatus.ACTIVE) {
            throw new AppException(ErrorCode.DEBT_NOT_ACTIVE);}

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_AMOUNT);}

        BigDecimal principal = debt.getRemainingPrincipal();
        if (principal == null) {
            principal = BigDecimal.ZERO;}

        BigDecimal interest = debt.getAccruedInterest();
        if (interest == null) {interest = BigDecimal.ZERO;}

        BigDecimal totalDebt = principal.add(interest);
        if (amount.compareTo(totalDebt) > 0) {
            throw new AppException(ErrorCode.PAYMENT_EXCEEDS_REMAINING);}
    }
}