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

    public AnalysisResponse(DtiResponse dti, InterestRatioResponse interestRatio,
                            OverdueRatioResponse overdueRatio, RepaymentTimeResponse repaymentTime) {
        this.dti = dti;
        this.interestRatio = interestRatio;
        this.overdue = overdueRatio;
        this.repaymentTime = repaymentTime;
    }
}