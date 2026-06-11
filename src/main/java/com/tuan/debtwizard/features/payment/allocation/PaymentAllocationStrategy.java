package com.tuan.debtwizard.features.payment.allocation;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.payment.dto.PaymentAllocationResult;

import java.math.BigDecimal;

public interface PaymentAllocationStrategy {
    PaymentAllocationResult allocate(Debt debt, BigDecimal amount);
}