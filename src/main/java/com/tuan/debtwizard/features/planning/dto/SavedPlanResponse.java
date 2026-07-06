package com.tuan.debtwizard.features.planning.dto;

import com.tuan.debtwizard.features.planning.model.RepaymentStrategy;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class SavedPlanResponse {

    private Long id;
    private RepaymentStrategy strategy;
    private String planName;
    private BigDecimal monthlyExtraPayment;
    private BigDecimal totalInterestPaid;
    private int payoffDurationMonths;
    private LocalDateTime savedAt;
    private List<SimulationMonthDto> schedule;
}
