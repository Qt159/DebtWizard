package com.tuan.debtwizard.features.planning.model;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SimulationMonth {
    private int monthIndex;
    private LocalDate date;

    private BigDecimal totalPayment = BigDecimal.ZERO;
    private BigDecimal extraPaymentUsed = BigDecimal.ZERO;
    private BigDecimal cashflowReleased = BigDecimal.ZERO;

    private List<SimulationPayment> payments = new ArrayList<>();

}