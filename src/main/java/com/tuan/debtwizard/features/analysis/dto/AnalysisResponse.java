package com.tuan.debtwizard.features.analysis.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalysisResponse {
    private DtiResponse dti;
    private InterestRatioResponse interestRatio;
    private OverdueRatioResponse overdue;
    private RepaymentTimeResponse repaymentTime;
}