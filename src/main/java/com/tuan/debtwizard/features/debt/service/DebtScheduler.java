package com.tuan.debtwizard.features.debt.service;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import com.tuan.debtwizard.features.interest.service.InterestAccrualService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DebtScheduler {

    private final DebtRepository debtRepository;
    private final DebtStateService debtStateService;
    private static final Logger log = LoggerFactory.getLogger(DebtScheduler.class);
    private final InterestAccrualService interestAccrualService;
    private static final int BATCH_SIZE = 100;
    public DebtScheduler(
            DebtRepository debtRepository,
            DebtStateService debtStateService,
            InterestAccrualService interestAccrualService) {
        this.debtRepository = debtRepository;
        this.debtStateService = debtStateService;
        this.interestAccrualService = interestAccrualService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void refreshDebts() {
        while (true){
            Page<Debt> debtPage = debtRepository.findByDeletedFalseAndStatusNot(
                    DebtStatus.PAID_OFF,
                    PageRequest.of(0, BATCH_SIZE)
            );
            if (debtPage.isEmpty()) {
                break;}
            processBatch(debtPage.getContent());
        }


    }
    @Transactional
    // Transaction chỉ tồn tại cho 100 bản ghi
    public void processBatch(List<Debt> debts) {
        for (Debt debt : debts) {
            try {
                interestAccrualService.accrueInterest(debt, LocalDate.now());
                debtStateService.refreshDebtStatus(debt);
            } catch (Exception e) {
                log.error("Lỗi xử lý debt id: {}", debt.getId(), e);
            }
        }
    }
}