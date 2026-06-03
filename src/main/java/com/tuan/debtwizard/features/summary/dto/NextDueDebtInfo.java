package com.tuan.debtwizard.features.summary.dto;

import com.tuan.debtwizard.features.debt.model.Debt;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class NextDueDebtInfo {
    private String lenderName;
    private LocalDate nextDueDate;
    private long daysUntilDue;
    public NextDueDebtInfo(String lenderName,  LocalDate nextDueDate, long daysUntilDue) {
        this.lenderName = lenderName;
        this.nextDueDate = nextDueDate;
        this.daysUntilDue = daysUntilDue;
    }

}
