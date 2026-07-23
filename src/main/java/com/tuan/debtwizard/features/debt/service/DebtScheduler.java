package com.tuan.debtwizard.features.debt.service;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
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
        int page = 0;
        while (true) {
            Page<Debt> debtPage = debtRepository.findByDeletedFalseAndStatusNot(
                    DebtStatus.PAID_OFF,
                    PageRequest.of(page, BATCH_SIZE)
            );
            if (debtPage.isEmpty()) {
                break;
            }
            debtBatchProcessor.processBatch(debtPage.getContent());
            if (!debtPage.hasNext()) {
                break;
            }
            page++;
        }
    }
    
}