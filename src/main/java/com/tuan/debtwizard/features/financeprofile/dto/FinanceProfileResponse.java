package com.tuan.debtwizard.features.financeprofile.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class FinanceProfileResponse {

    private Long id;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyEssentialExpenses;
    private LocalDateTime updatedAt;

    public FinanceProfileResponse(Long id,BigDecimal monthlyIncome,
                                  BigDecimal monthlyEssentialExpenses, LocalDateTime updatedAt) {
        this.id = id;
        this.monthlyIncome = monthlyIncome;
        this.monthlyEssentialExpenses = monthlyEssentialExpenses;
        this.updatedAt = updatedAt;
    }
}
