package com.tuan.debtwizard.features.planning.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class RepaymentMonthDto {
    private int monthIndex;
    private LocalDate date;

    private BigDecimal totalPayment;
    private BigDecimal extraPaymentUsed;
    private BigDecimal cashflowReleased;

    private List<DebtPaymentDto> debts;

}