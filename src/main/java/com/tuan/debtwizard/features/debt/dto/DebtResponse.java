package com.tuan.debtwizard.features.debt.dto;

import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.model.DebtType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class DebtResponse {
    private Long id;
    private String lenderName;
    private BigDecimal totalPrincipal;
    private BigDecimal remainingPrincipal;
    private BigDecimal interestRate;
    private BigDecimal monthlyPayment;
    private Integer termMonths;
    private LocalDate startDate;
    private LocalDate dueDay;
    private DebtStatus status;
    private DebtType debtType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}