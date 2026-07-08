package com.tuan.debtwizard.features.planning.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import com.tuan.debtwizard.features.planning.dto.CompareRequest;
import com.tuan.debtwizard.features.planning.dto.CompareResponse;
import com.tuan.debtwizard.features.planning.dto.DebtPaymentDetailDto;
import com.tuan.debtwizard.features.planning.dto.PlanComparisonDto;
import com.tuan.debtwizard.features.planning.dto.SavePlanRequest;
import com.tuan.debtwizard.features.planning.dto.SavedPlanResponse;
import com.tuan.debtwizard.features.planning.dto.SimulationMonthDto;
import com.tuan.debtwizard.features.planning.mapper.SavedPlanMapper;
import com.tuan.debtwizard.features.planning.mapper.SnapshotMapper;
import com.tuan.debtwizard.features.planning.model.DebtSnapshot;
import com.tuan.debtwizard.features.planning.model.PlanDebtPayment;
import com.tuan.debtwizard.features.planning.model.PlanMonthlySchedule;
import com.tuan.debtwizard.features.planning.model.SavedPlan;
import com.tuan.debtwizard.features.planning.repository.SavedPlanRepository;
import com.tuan.debtwizard.features.user.model.User;
import com.tuan.debtwizard.features.user.repository.UserRepository;
import com.tuan.debtwizard.features.planning.helper.SimulationHelper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlanningService {

    private final SimulationEngine simulationEngine;
    private final DebtRepository debtRepository;
    private final UserRepository userRepository;
    private final SavedPlanRepository savedPlanRepository;
    private final SnapshotMapper snapshotMapper;
    private final SavedPlanMapper savedPlanMapper;
    private final SimulationHelper simulationHelper;

    public PlanningService(SimulationEngine simulationEngine,
                           DebtRepository debtRepository,
                           UserRepository userRepository,
                           SavedPlanRepository savedPlanRepository,
                           SnapshotMapper snapshotMapper,
                           SavedPlanMapper savedPlanMapper,
                           SimulationHelper simulationHelper) {
        this.simulationEngine = simulationEngine;
        this.debtRepository = debtRepository;
        this.userRepository = userRepository;
        this.savedPlanRepository = savedPlanRepository;
        this.snapshotMapper = snapshotMapper;
        this.savedPlanMapper = savedPlanMapper;
        this.simulationHelper = simulationHelper;
    }

    // Compare không lưu DB
    public CompareResponse comparePlans(CompareRequest request, UserDetails userDetails) {
        User user = getUser(userDetails.getUsername());
        List<Debt> debts = loadAndVerifyDebts(request.getDebtIds(), user.getUsername());
        List<DebtSnapshot> snapshots = snapshotMapper.toSnapshots(debts);

        // Tính ngưỡng tối đa: thu nhập - chi tiêu - tổng minimum payments
        BigDecimal maxAllowed = simulationHelper.calculateMonthlyExtraBudget(user, snapshots);

        // Validate: user không được nhập vượt ngưỡng
        if (request.getMonthlyExtraPayment().compareTo(maxAllowed) > 0) {
            throw new AppException(ErrorCode.EXTRA_PAYMENT_EXCEEDS_MAX);
        }

        List<DebtSnapshot> forFirst = new ArrayList<>();
        List<DebtSnapshot> forSecond = new ArrayList<>();
        for (DebtSnapshot s : snapshots) {
            forFirst.add(new DebtSnapshot(s));
            forSecond.add(new DebtSnapshot(s));
        }

        CompareResponse response = new CompareResponse();
        response.setMaxAllowedExtraPayment(maxAllowed);
        response.setFirstPlan(simulationEngine.simulate(forFirst, request.getFirstStrategy(), request.getMonthlyExtraPayment()));
        response.setSecondPlan(simulationEngine.simulate(forSecond, request.getSecondStrategy(), request.getMonthlyExtraPayment()));
        return response;
    }

    
    // Save / Get / Delete
    @Transactional
    public SavedPlanResponse savePlan(SavePlanRequest request, UserDetails userDetails) {
        User user = getUser(userDetails.getUsername());
        List<Debt> debts = loadAndVerifyDebts(request.getDebtIds(), user.getUsername());
        List<DebtSnapshot> snapshots = snapshotMapper.toSnapshots(debts);

        // Validate: không vượt ngưỡng tối đa
        BigDecimal maxAllowed = simulationHelper.calculateMonthlyExtraBudget(user, snapshots);
        if (request.getMonthlyExtraPayment().compareTo(maxAllowed) > 0) {
            throw new AppException(ErrorCode.EXTRA_PAYMENT_EXCEEDS_MAX);
        }

        PlanComparisonDto result = simulationEngine.simulate(
                snapshots, request.getStrategy(), request.getMonthlyExtraPayment());
        savedPlanRepository.findByUserId(user.getId()).ifPresent(savedPlanRepository::delete);
        savedPlanRepository.flush();

        SavedPlan plan = new SavedPlan();
        plan.setUser(user);
        plan.setStrategy(request.getStrategy());
        plan.setPlanName(result.getPlanName());
        plan.setMonthlyExtraPayment(request.getMonthlyExtraPayment());
        plan.setTotalInterestPaid(result.getTotalInterestPaid());
        plan.setPayoffDurationMonths(result.getPayoffDurationMonths());
        plan.setMonthlySchedules(buildSchedules(plan, result.getSchedule(), debts));

        return savedPlanMapper.toResponse(savedPlanRepository.save(plan));
    }

    @Transactional(readOnly = true)
    public SavedPlanResponse getSavedPlan(UserDetails userDetails) {
        User user = getUser(userDetails.getUsername());
        SavedPlan plan = savedPlanRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));
        return savedPlanMapper.toResponse(plan);
    }

    @Transactional
    public void deleteSavedPlan(UserDetails userDetails) {
        User user = getUser(userDetails.getUsername());
        SavedPlan plan = savedPlanRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PLAN_NOT_FOUND));
        savedPlanRepository.delete(plan);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
    @Transactional(readOnly = true)
    protected List<Debt> loadAndVerifyDebts(List<Long> debtIds, String username) {
        List<Debt> debts = debtRepository.findAllByIdWithUser(debtIds);
        for (Debt debt : debts) {
            if (!debt.getUser().getUsername().equals(username)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }
        return debts;
    }

    private List<PlanMonthlySchedule> buildSchedules(SavedPlan plan,
                                                      List<SimulationMonthDto> months,
                                                      List<Debt> debts) {
        List<PlanMonthlySchedule> schedules = new ArrayList<>();
        for (SimulationMonthDto month : months) {
            PlanMonthlySchedule schedule = new PlanMonthlySchedule();
            schedule.setSavedPlan(plan);
            schedule.setMonthIndex(month.getMonthIndex());
            schedule.setDate(month.getDate());
            schedule.setTotalPayment(month.getTotalPayment());
            schedule.setExtraPaymentUsed(month.getExtraPaymentUsed());
            schedule.setCashflowReleased(month.getCashflowReleased());
            schedule.setDebtPayments(buildDebtPayments(schedule, month.getPayments(), debts));
            schedules.add(schedule);
        }
        return schedules;
    }

    private List<PlanDebtPayment> buildDebtPayments(PlanMonthlySchedule schedule,
                                                     List<DebtPaymentDetailDto> payments,
                                                     List<Debt> debts) {
        List<PlanDebtPayment> result = new ArrayList<>();
        for (DebtPaymentDetailDto payment : payments) {
            Debt debt = debts.stream()
                    .filter(d -> d.getId().equals(payment.getDebtId()))
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.DEBT_NOT_FOUND));

            PlanDebtPayment pdp = new PlanDebtPayment();
            pdp.setSchedule(schedule);
            pdp.setDebt(debt);
            pdp.setDebtName(payment.getDebtName());
            pdp.setMinimumPaid(payment.getMinimumPaid());
            pdp.setExtraPaid(payment.getExtraPaid());
            pdp.setPrincipalPaid(payment.getPrincipalPaid());
            pdp.setInterestPaid(payment.getInterestPaid());
            pdp.setRemainingBalance(payment.getRemainingBalance());
            pdp.setPaidOff(payment.isPaidOff());
            result.add(pdp);
        }
        return result;
    }
}
