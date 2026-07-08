package com.tuan.debtwizard.features.planning.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CompareResponse {
    /* Ngưỡng tối đa user được phép nhập cho monthlyExtraPayment.
    Tính bằng: income - expense - tổng minimumPayment của các khoản nợ được chọn
    */
    private BigDecimal maxAllowedExtraPayment;
    private PlanComparisonDto firstPlan;
    private PlanComparisonDto secondPlan;
}