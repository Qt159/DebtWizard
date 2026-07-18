package com.tuan.debtwizard.features.debt.dto;

import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.model.DebtType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class DebtListItemResponse {
    private Long id;
    private String lenderName;
    private BigDecimal totalPrincipal;
    private BigDecimal remainingPrincipal;
    private DebtStatus status;
    private DebtType debtType;
    private Integer dueDay;
    private LocalDate nextDueDate;
    private BigDecimal interestRate;
}