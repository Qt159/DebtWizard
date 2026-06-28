package com.tuan.debtwizard.features.planning.dto;

import com.tuan.debtwizard.features.planning.model.RepaymentStrategy;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class SimulationRequest {
    private List<Long> debtIds;
    private RepaymentStrategy strategy;
    private BigDecimal availableExtraPayment;

}