package com.tuan.debtwizard.features.planning.service;

import com.tuan.debtwizard.features.planning.helper.PaymentHelper;
import com.tuan.debtwizard.features.planning.helper.SimulationHelper;
import com.tuan.debtwizard.features.planning.model.DebtSnapshot;
import com.tuan.debtwizard.features.planning.model.RepaymentStrategy;
import com.tuan.debtwizard.features.planning.model.SimulationMonth;
import com.tuan.debtwizard.features.planning.model.SimulationResult;
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

    public SimulationEngine(PaymentHelper paymentHelper, SimulationHelper simulationHelper, MinimizeInterestStrategy minimizeInterestStrategy, ImproveCashflowStrategy improveCashflowStrategy) {
        this.paymentHelper = paymentHelper;
        this.simulationHelper = simulationHelper;
        this.minimizeInterestStrategy = minimizeInterestStrategy;
        this.improveCashflowStrategy = improveCashflowStrategy;
    }


    public SimulationResult simulate(List<DebtSnapshot> snapshots, RepaymentStrategy strategyType,
            BigDecimal monthlyExtraPayment) {
        DebtSelectionStrategy strategy = getStrategy(strategyType);
        int monthIndex = 0;
        BigDecimal totalInterest = BigDecimal.ZERO;
        List<SimulationMonth> months = new ArrayList<>();
        while (simulationHelper.hasActiveDebt(snapshots)) {
            monthIndex++;
            List<DebtSnapshot> activeDebts = simulationHelper.getActiveDebts(snapshots);

            paymentHelper.resetMonthlyTracking(activeDebts);
            paymentHelper.applyMinimumPayments(activeDebts);
            DebtSnapshot target = strategy.selectTargetDebt(activeDebts);
            BigDecimal extraUsed = paymentHelper.applyExtraPayment(monthlyExtraPayment, target);

            monthlyExtraPayment = monthlyExtraPayment.subtract(extraUsed);
            BigDecimal interest = paymentHelper.applyMonthlyInterest(activeDebts);
            totalInterest = totalInterest.add(interest);

            BigDecimal released = paymentHelper.releaseCashflow(activeDebts);
            monthlyExtraPayment = monthlyExtraPayment.add(released);

            BigDecimal totalPayment = simulationHelper.calculateTotalPayment(activeDebts);
            SimulationMonth month = new SimulationMonth();
            month.setMonthIndex(monthIndex);
            month.setDate(LocalDate.now().plusMonths(monthIndex));
            month.setTotalPayment(totalPayment);
            month.setExtraPaymentUsed(extraUsed);
            month.setCashflowReleased(released);
            month.setPayments(new ArrayList<>());

            months.add(month);
        }

        SimulationResult result = new SimulationResult();
        result.setMonths(months);
        result.setPayoffDurationMonths(monthIndex);
        result.setTotalInterestPaid(totalInterest);

        return result;
    }

    private DebtSelectionStrategy getStrategy(RepaymentStrategy type) {
        return switch (type) {
            case MINIMIZE_INTEREST -> minimizeInterestStrategy;
            case IMPROVE_CASHFLOW -> improveCashflowStrategy;
        };
    }
}