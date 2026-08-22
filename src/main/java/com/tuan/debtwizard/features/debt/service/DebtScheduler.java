package com.tuan.debtwizard.features.debt.service;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DebtScheduler {

    private final DebtRepository debtRepository;
    private final DebtBatchProcessor debtBatchProcessor;
    private static final int BATCH_SIZE = 100;
    public DebtScheduler(
            DebtRepository debtRepository,
            DebtBatchProcessor debtBatchProcessor) {
        this.debtRepository = debtRepository;
        this.debtBatchProcessor = debtBatchProcessor;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void refreshDebts() {
        Long lastId = 0L;
        while (true) {
            List<Debt> debts = debtRepository.findTop100ByIdGreaterThanAndDeletedFalseAndStatusNotOrderByIdAsc(
                    lastId,
                    DebtStatus.PAID_OFF);
            if (debts.isEmpty()) {
                break;
            }
            debtBatchProcessor.processBatch(debts);
            lastId = debts.get(debts.size() - 1).getId();
        }
    }
    
}