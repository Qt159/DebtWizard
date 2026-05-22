package com.tuan.debtwizard.features.debt.mapper;

import com.tuan.debtwizard.features.debt.dto.DebtListItem;
import com.tuan.debtwizard.features.debt.dto.DebtRequest;
import com.tuan.debtwizard.features.debt.dto.DebtResponse;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import org.springframework.stereotype.Component;

@Component
public class DebtMapper {


    public Debt toEntity(DebtRequest debtRequest) {
        Debt debt = new Debt();
        debt.setLenderName(debtRequest.getLenderName());
        debt.setTotalPrincipal(debtRequest.getTotalPrincipal());
        // mới tạo thì remaining = total
        debt.setRemainingPrincipal(debtRequest.getTotalPrincipal());
        debt.setMonthlyPayment(debtRequest.getMonthlyPayment());
        debt.setDebtType(debtRequest.getDebtType());
        debt.setInterestRate(debtRequest.getInterestRate());
        debt.setTermMonths(debtRequest.getTermMonths());
        debt.setStartDate(debtRequest.getStartDate());
        debt.setDueDay(debtRequest.getDueDay());
        debt.setStatus(DebtStatus.ACTIVE);
        return debt;
    }
    public DebtResponse toResponse(Debt debt) {
        DebtResponse response = new DebtResponse();
        response.setId(debt.getId());
        response.setLenderName(debt.getLenderName());
        response.setTotalPrincipal(debt.getTotalPrincipal());
        response.setRemainingPrincipal(debt.getRemainingPrincipal());
        response.setInterestRate(debt.getInterestRate());
        response.setMonthlyPayment(debt.getMonthlyPayment());
        response.setTermMonths(debt.getTermMonths());
        response.setStartDate(debt.getStartDate());
        response.setDueDay(debt.getDueDay());
        response.setStatus(debt.getStatus());
        response.setDebtType(debt.getDebtType());
        response.setCreatedAt(debt.getCreatedAt());
        response.setUpdatedAt(debt.getUpdatedAt());
        return response;
    }

    public DebtListItem toListItem(Debt debt) {
        DebtListItem item = new DebtListItem();
        item.setId(debt.getId());
        item.setLenderName(debt.getLenderName());
        item.setTotalPrincipal(debt.getTotalPrincipal());
        item.setRemainingPrincipal(debt.getRemainingPrincipal());
        item.setStatus(debt.getStatus());
        return item;
    }
}