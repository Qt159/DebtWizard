package com.tuan.debtwizard.features.planning.service;

import com.tuan.debtwizard.features.planning.dto.DebtPaymentDetailDto;
import com.tuan.debtwizard.features.planning.dto.PlanComparisonDto;
import com.tuan.debtwizard.features.planning.dto.SimulationMonthDto;
import com.tuan.debtwizard.features.planning.helper.PaymentHelper;
import com.tuan.debtwizard.features.planning.helper.SimulationHelper;
import com.tuan.debtwizard.features.planning.model.DebtSnapshot;
import com.tuan.debtwizard.features.planning.model.RepaymentStrategy;
import com.tuan.debtwizard.features.planning.service.strategy.DebtSelectionStrategy;
import com.tuan.debtwizard.features.planning.service.strategy.ImproveCashflowStrategy;
import com.tuan.debtwizard.features.planning.service.strategy.MinimizeInterestStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class SimulationEngine {

    private final PaymentHelper paymentHelper;
    private final SimulationHelper simulationHelper;
    private final MinimizeInterestStrategy minimizeInterestStrategy;
    private final ImproveCashflowStrategy improveCashflowStrategy;

    public SimulationEngine(PaymentHelper paymentHelper,
                            SimulationHelper simulationHelper,
                            MinimizeInterestStrategy minimizeInterestStrategy,
                            ImproveCashflowStrategy improveCashflowStrategy) {
        this.paymentHelper = paymentHelper;
        this.simulationHelper = simulationHelper;
        this.minimizeInterestStrategy = minimizeInterestStrategy;
        this.improveCashflowStrategy = improveCashflowStrategy;
    }

    /**
     * Chạy simulation và trả thẳng PlanComparisonDto.
     *
     * monthlyExtraPayment: số tiền trả thêm cố định mỗi tháng do user nhập.
     * snowballBonus: tiền được giải phóng từ các khoản nợ đã trả hết, cộng dồn qua từng tháng.
     */
    public PlanComparisonDto simulate(List<DebtSnapshot> snapshots,
                                      RepaymentStrategy strategyType,
                                      BigDecimal monthlyExtraPayment) {
        DebtSelectionStrategy strategy = resolveStrategy(strategyType);

        int monthIndex = 0;
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal snowballBonus = BigDecimal.ZERO; // tích lũy từ cashflow released
        List<SimulationMonthDto> schedule = new ArrayList<>();

        while (simulationHelper.hasActiveDebt(snapshots)) {
            if (++monthIndex > 600) break;

            List<DebtSnapshot> activeDebts = simulationHelper.getActiveDebts(snapshots);
            paymentHelper.resetMonthlyTracking(activeDebts);

            BigDecimal interest = paymentHelper.applyMonthlyInterest(activeDebts);
            totalInterest = totalInterest.add(interest);

            paymentHelper.applyMinimumPayments(activeDebts);

            // Tổng extra tháng này = extra gốc của user + tích lũy snowball từ tháng trước
            BigDecimal totalExtraThisMonth = monthlyExtraPayment.add(snowballBonus);

            DebtSnapshot target = strategy.selectTargetDebt(activeDebts, totalExtraThisMonth);
            BigDecimal extraUsed = paymentHelper.applyExtraPayment(totalExtraThisMonth, target);

            // Khi một khoản nợ trả hết, minimumPayment của nó được giải phóng vào snowball
            BigDecimal released = paymentHelper.releaseCashflow(activeDebts);
            snowballBonus = snowballBonus.add(released);

            BigDecimal totalPayment = simulationHelper.calculateTotalPayment(activeDebts);

            List<DebtPaymentDetailDto> payments = new ArrayList<>();
            for (DebtSnapshot debt : activeDebts) {
                DebtPaymentDetailDto p = new DebtPaymentDetailDto();
                p.setDebtId(debt.getDebtId());
                p.setDebtName(debt.getDebtName());
                p.setMinimumPaid(debt.getCurrentMinimumPaid());
                p.setExtraPaid(debt.getCurrentExtraPaid());
                p.setPrincipalPaid(debt.getCurrentPrincipalPaid());
                p.setInterestPaid(debt.getCurrentInterestPaid());
                p.setTotalPaid(debt.getPaymentThisMonth());
                p.setRemainingBalance(debt.getBalance());
                p.setPaidOff(debt.isPaidOff());
                payments.add(p);
            }

            SimulationMonthDto month = new SimulationMonthDto();
            month.setMonthIndex(monthIndex);
            month.setDate(LocalDate.now().plusMonths(monthIndex));
            month.setTotalPayment(totalPayment);
            month.setExtraPaymentUsed(extraUsed);
            month.setCashflowReleased(released);
            month.setPayments(payments);
            schedule.add(month);
        }

        PlanComparisonDto dto = new PlanComparisonDto();
        dto.setStrategy(strategyType);
        dto.setPlanName(strategyType.getDisplayName());
        dto.setTotalInterestPaid(totalInterest);
        dto.setPayoffDurationMonths(monthIndex);
        dto.setSchedule(schedule);
        return dto;
    }

    private DebtSelectionStrategy resolveStrategy(RepaymentStrategy type) {
        return switch (type) {
            case MINIMIZE_INTEREST -> minimizeInterestStrategy;
            case IMPROVE_CASHFLOW -> improveCashflowStrategy;
        };
    }
}
