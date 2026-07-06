package com.tuan.debtwizard.features.planning.mapper;

import com.tuan.debtwizard.features.planning.dto.DebtPaymentDetailDto;
import com.tuan.debtwizard.features.planning.dto.SimulationMonthDto;
import com.tuan.debtwizard.features.planning.model.SimulationMonth;
import com.tuan.debtwizard.features.planning.model.SimulationPayment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RepaymentMapper {

    public List<SimulationMonthDto> toSimulationMonthDtos(List<SimulationMonth> months) {
        List<SimulationMonthDto> result = new ArrayList<>();
        for (SimulationMonth month : months) {
            result.add(toSimulationMonthDto(month));
        }
        return result;
    }

    public SimulationMonthDto toSimulationMonthDto(SimulationMonth month) {
        SimulationMonthDto dto = new SimulationMonthDto();
        dto.setMonthIndex(month.getMonthIndex());
        dto.setDate(month.getDate());
        dto.setTotalPayment(month.getTotalPayment());
        dto.setExtraPaymentUsed(month.getExtraPaymentUsed());
        dto.setCashflowReleased(month.getCashflowReleased());
        dto.setPayments(toDebtPaymentDetailDtos(month.getPayments()));
        return dto;
    }

    public List<DebtPaymentDetailDto> toDebtPaymentDetailDtos(List<SimulationPayment> payments) {
        List<DebtPaymentDetailDto> result = new ArrayList<>();
        for (SimulationPayment payment : payments) {
            result.add(toDebtPaymentDetailDto(payment));
        }
        return result;
    }

    public DebtPaymentDetailDto toDebtPaymentDetailDto(SimulationPayment payment) {
        DebtPaymentDetailDto dto = new DebtPaymentDetailDto();
        dto.setDebtId(payment.getDebtId());
        dto.setDebtName(payment.getDebtName());
        dto.setMinimumPaid(payment.getMinimumPaid());
        dto.setExtraPaid(payment.getExtraPaid());
        dto.setPrincipalPaid(payment.getPrincipalPaid());
        dto.setInterestPaid(payment.getInterestPaid());
        dto.setTotalPaid(payment.getPaymentThisMonth());
        dto.setRemainingBalance(payment.getRemainingBalance());
        dto.setPaidOff(payment.isPaidOff());
        return dto;
    }
}
