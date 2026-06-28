package com.tuan.debtwizard.features.planning.dto;

import com.tuan.debtwizard.features.planning.model.RepaymentStrategy;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CompareRequest {
    private List<Long> debtIds;
    private BigDecimal monthlyExtraPayment;
    private RepaymentStrategy firstStrategy;
    private RepaymentStrategy secondStrategy;
}