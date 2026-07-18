package com.tuan.debtwizard.features.planning.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.planning.dto.PlanComparisonDto;
import com.tuan.debtwizard.features.planning.dto.SimulationMonthDto;
import com.tuan.debtwizard.features.planning.helper.PaymentHelper;
import com.tuan.debtwizard.features.planning.helper.SimulationHelper;
import com.tuan.debtwizard.features.planning.mapper.SimulationMapper;
import com.tuan.debtwizard.features.planning.model.DebtSnapshot;
import com.tuan.debtwizard.features.planning.model.RepaymentStrategy;
import com.tuan.debtwizard.features.planning.service.strategy.DebtSelectionStrategy;
import com.tuan.debtwizard.features.planning.service.strategy.ImproveCashflowStrategy;
import com.tuan.debtwizard.features.planning.service.strategy.MinimizeInterestStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class SimulationEngine {

    private final PaymentHelper paymentHelper;
    private final SimulationHelper simulationHelper;
    private final MinimizeInterestStrategy minimizeInterestStrategy;
    private final ImproveCashflowStrategy improveCashflowStrategy;
    private final SimulationMapper simulationMapper;

    public SimulationEngine(
            PaymentHelper paymentHelper,
            SimulationHelper simulationHelper,
            MinimizeInterestStrategy minimizeInterestStrategy,
            ImproveCashflowStrategy improveCashflowStrategy,
            SimulationMapper simulationMapper) {

        this.paymentHelper = paymentHelper;
        this.simulationHelper = simulationHelper;
        this.minimizeInterestStrategy = minimizeInterestStrategy;
        this.improveCashflowStrategy = improveCashflowStrategy;
        this.simulationMapper = simulationMapper;
    }

    /*
     Chạy simulation và trả về kết quả của một repayment strategy.
     monthlyExtraPayment: số tiền user chủ động trả thêm mỗi tháng.
     snowballBonus: phần minimum payment được giải phóng từ các khoản nợ đã tất toán và được cộng dồn vào các tháng tiếp theo.
     */
    public PlanComparisonDto simulate(
            List<DebtSnapshot> snapshots,
            RepaymentStrategy strategyType,
            BigDecimal monthlyExtraPayment) {

        DebtSelectionStrategy selectionStrategy = resolveStrategy(strategyType);

        int monthIndex = 0;
        BigDecimal totalInterest = BigDecimal.ZERO;
        BigDecimal snowballBonus = BigDecimal.ZERO;
        List<SimulationMonthDto> schedule = new ArrayList<>();

        while (simulationHelper.hasActiveDebt(snapshots)) {

            if (++monthIndex > 600) {
                throw new AppException(ErrorCode.SIMULATION_FAILED);
            }

            List<DebtSnapshot> activeDebts = simulationHelper.getActiveDebts(snapshots);

            paymentHelper.resetMonthlyTracking(activeDebts);

            BigDecimal interest = paymentHelper.applyMonthlyInterest(activeDebts);
            totalInterest = totalInterest.add(interest);

            paymentHelper.applyMinimumPayments(activeDebts);

            // Extra payment của tháng hiện tại = extra user nhập + snowball tích lũy
            BigDecimal totalExtraThisMonth = monthlyExtraPayment.add(snowballBonus);

            DebtSnapshot target =
                    selectionStrategy.selectTargetDebt(activeDebts, totalExtraThisMonth);

            BigDecimal extraUsed =
                    paymentHelper.applyExtraPayment(totalExtraThisMonth, target);

            // Nếu có khoản nợ được tất toán thì giải phóng minimum payment
            BigDecimal released = paymentHelper.releaseCashflow(activeDebts);
            snowballBonus = snowballBonus.add(released);

            BigDecimal totalPayment =
                    simulationHelper.calculateTotalPayment(activeDebts);

            SimulationMonthDto month = simulationMapper.toSimulationMonth(
                    monthIndex,
                    totalPayment,
                    extraUsed,
                    released,
                    simulationMapper.toPaymentDetails(activeDebts));
            schedule.add(month);
        }

        return simulationMapper.toPlanComparison(
                strategyType,
                totalInterest,
                monthIndex,
                schedule
        );
    }

    private DebtSelectionStrategy resolveStrategy(RepaymentStrategy strategyType) {
        return switch (strategyType) {
            case MINIMIZE_INTEREST -> minimizeInterestStrategy;
            case IMPROVE_CASHFLOW -> improveCashflowStrategy;
        };
    }
}