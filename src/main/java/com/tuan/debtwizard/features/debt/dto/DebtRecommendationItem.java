package com.tuan.debtwizard.features.debt.dto;

import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.model.DebtType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DebtRecommendationItem {
    private Integer priority;
    private Long debtId;
    private String lenderName;
    private BigDecimal remainingPrincipal;
    private BigDecimal interestRate;
    private String recommendationReason;
}