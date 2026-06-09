package com.tuan.debtwizard.features.payment.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.payment.dto.PaymentAllocationResult;
import com.tuan.debtwizard.features.payment.model.PaymentApplicationRule;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentAllocationService {

    public PaymentAllocationResult allocate(Debt debt, BigDecimal amount) {
        validate(debt, amount);
        PaymentApplicationRule rule =
                debt.getInterestConfig().getPaymentApplicationRule();
        if (rule == null) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_RULE);}
        BigDecimal interest = debt.getAccruedInterest();
        if (interest == null) {
            interest = BigDecimal.ZERO;}
        BigDecimal principal = debt.getRemainingPrincipal();
        if (principal == null) {
            principal = BigDecimal.ZERO;
        }
        BigDecimal remainingPayment = amount;
        BigDecimal interestPaid = BigDecimal.ZERO;
        BigDecimal principalPaid = BigDecimal.ZERO;

        if (rule == PaymentApplicationRule.INTEREST_FIRST) {

            interestPaid = interest.min(remainingPayment);
            interest = interest.subtract(interestPaid);
            remainingPayment = remainingPayment.subtract(interestPaid);

            principalPaid = principal.min(remainingPayment);
            principal = principal.subtract(principalPaid);
            remainingPayment = remainingPayment.subtract(principalPaid);

        } else {
            principalPaid = principal.min(remainingPayment);
            principal = principal.subtract(principalPaid);
            remainingPayment = remainingPayment.subtract(principalPaid);

            interestPaid = interest.min(remainingPayment);
            interest = interest.subtract(interestPaid);
            remainingPayment = remainingPayment.subtract(interestPaid);
        }

        debt.setRemainingPrincipal(principal);
        debt.setAccruedInterest(interest);

        if (principal.compareTo(BigDecimal.ZERO) == 0
                && interest.compareTo(BigDecimal.ZERO) == 0) {
            debt.setStatus(DebtStatus.PAID_OFF);
        }

        return new PaymentAllocationResult(principalPaid, interestPaid, remainingPayment);}

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