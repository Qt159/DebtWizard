package com.tuan.debtwizard.features.debt.service;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.service.interest.InterestAccrualService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DebtBatchProcessor {
    private static final Logger log =LoggerFactory.getLogger(DebtBatchProcessor.class);

    private final InterestAccrualService interestAccrualService;
    private final DebtStateService debtStateService;

    public DebtBatchProcessor(
            InterestAccrualService interestAccrualService,
            DebtStateService debtStateService) {

        this.interestAccrualService = interestAccrualService;
        this.debtStateService = debtStateService;
    }


    @Transactional
    public void processBatch(List<Debt> debts) {
        for (Debt debt : debts) {
            try {
                interestAccrualService.accrueInterest(debt, LocalDate.now());
                debtStateService.refreshDebtStatus(debt);
            } catch (Exception e) {
                log.error("Failed processing debt id: {}", debt.getId(), e
                );
            }
        }
    }
}