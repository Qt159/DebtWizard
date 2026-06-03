package com.tuan.debtwizard.features.debt.dto;

import com.tuan.debtwizard.features.debt.model.DebtStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DebtListItemResponse {
    private Long id;
    private String lenderName;
    private BigDecimal totalPrincipal;
    private BigDecimal remainingPrincipal;
    private DebtStatus status;
    private Integer dueDay;
}