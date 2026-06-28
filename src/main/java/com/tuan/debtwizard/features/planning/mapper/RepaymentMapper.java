package com.tuan.debtwizard.features.planning.mapper;

import com.tuan.debtwizard.features.planning.dto.DebtPaymentDto;
import com.tuan.debtwizard.features.planning.dto.RepaymentMonthDto;
import com.tuan.debtwizard.features.planning.model.DebtSnapshot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class RepaymentMapper {

    public RepaymentMonthDto toRepaymentMonth(
            int monthIndex,
            LocalDate date,
            BigDecimal totalPayment,
            BigDecimal extraPaymentUsed,
            BigDecimal cashflowReleased,
            List<DebtPaymentDto> debtPayments
    ) {
        RepaymentMonthDto month = new RepaymentMonthDto();
        month.setMonthIndex(monthIndex);
        month.setDate(date);
        month.setTotalPayment(totalPayment);
        month.setExtraPaymentUsed(extraPaymentUsed);
        month.setCashflowReleased(cashflowReleased);
        month.setDebts(debtPayments);
        return month;
    }

    public List<DebtPaymentDto> toDebtPaymentDtos(List<DebtSnapshot> snapshots) {
        List<DebtPaymentDto> result = new ArrayList<>();
        for (DebtSnapshot snapshot : snapshots) {
            result.add(toDebtPaymentDto(snapshot));
        }
        return result;
    }

    public DebtPaymentDto toDebtPaymentDto(DebtSnapshot snapshot) {
        DebtPaymentDto dto = new DebtPaymentDto();

        dto.setDebtId(snapshot.getDebtId());
        dto.setDebtName(snapshot.getDebtName());

        dto.setMinimumPaid(snapshot.getCurrentMinimumPaid());
        dto.setExtraPaid(snapshot.getCurrentExtraPaid());
        dto.setPrincipalPaid(snapshot.getCurrentPrincipalPaid());
        dto.setInterestPaid(snapshot.getCurrentInterestPaid());

        dto.setRemainingBalance(snapshot.getBalance());
        dto.setPaidOff(snapshot.isPaidOff());

        return dto;
    }
}