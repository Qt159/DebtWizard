package com.tuan.debtwizard.features.planning.dto;

import com.tuan.debtwizard.features.planning.model.RepaymentStrategy;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class RepaymentPlanDto {
    private Long id;
    private RepaymentStrategy strategy;
    private BigDecimal totalPrincipalPaid;
    private BigDecimal totalInterestPaid;

    private int payoffDurationMonths;
    private List<RepaymentMonthDto> months;

}