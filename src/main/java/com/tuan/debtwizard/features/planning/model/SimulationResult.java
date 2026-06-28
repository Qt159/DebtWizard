package com.tuan.debtwizard.features.planning.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SimulationResult {
    private RepaymentStrategy strategy;
    private BigDecimal totalPrincipalPaid = BigDecimal.ZERO;
    private BigDecimal totalInterestPaid = BigDecimal.ZERO;
    private int payoffDurationMonths;
    private List<SimulationMonth> months = new ArrayList<>();

}