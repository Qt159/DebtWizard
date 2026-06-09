package com.tuan.debtwizard.features.debt.service;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import com.tuan.debtwizard.features.interest.service.InterestAccrualService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DebtScheduler {

    private final DebtRepository debtRepository;
    private final DebtStateService debtStateService;
    private final InterestAccrualService interestAccrualService;

    public DebtScheduler(
            DebtRepository debtRepository,
            DebtStateService debtStateService,
            InterestAccrualService interestAccrualService) {
        this.debtRepository = debtRepository;
        this.debtStateService = debtStateService;
        this.interestAccrualService = interestAccrualService;
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void refreshDebts() {
        List<Debt> debts =
                debtRepository.findByDeletedFalseAndStatusNot(DebtStatus.PAID_OFF);
        for (Debt debt : debts) {
            interestAccrualService.accrueInterest(debt, LocalDate.now());
            debtStateService.refreshDebtStatus(debt);
        }
    }
}