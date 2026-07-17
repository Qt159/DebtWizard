package com.tuan.debtwizard.features.dashboard.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

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
    public NextDueDebtInfo(){}


}
