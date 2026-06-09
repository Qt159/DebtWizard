package com.tuan.debtwizard.features.debt.dto;

import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.model.DebtType;
import com.tuan.debtwizard.features.interest.model.InterestCalculationMethod;
import com.tuan.debtwizard.features.interest.model.InterestRatePeriod;
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

    private BigDecimal expectedMonthlyPayment;
    private BigDecimal accruedInterest;
    private BigDecimal totalOutstanding;//phần nợ còn+ lãi

    private Integer termMonths;
    private LocalDate startDate;
    private LocalDate nextDueDate;
    private LocalDate lastPaymentDate;
    private Integer dueDay;
    private DebtStatus status;
    private DebtType debtType;
    private BigDecimal interestRate;
    private InterestCalculationMethod interestCalculationMethod;
    private InterestRatePeriod interestRatePeriod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    }