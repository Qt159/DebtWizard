package com.tuan.debtwizard.features.debt.service;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DebtScheduler {
    private final DebtRepository debtRepository;
    private final DebtStateService debtStateService;

    public DebtScheduler(DebtRepository debtRepository, DebtStateService debtStateService) {
        this.debtRepository = debtRepository;
        this.debtStateService = debtStateService;}

    @Transactional
        @Scheduled(cron = "0 0 0 * * *")
        public void refreshOverdueDebts() {
            List<Debt> debts = debtRepository.findByDeletedFalseAndStatusNot(DebtStatus.PAID_OFF);
            for (Debt debt : debts) {
                debtStateService.refreshDebtStatus(debt);
                debtRepository.save(debt);
            }
        }
    }

