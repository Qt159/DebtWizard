package com.tuan.debtwizard.features.planning.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import com.tuan.debtwizard.features.planning.dto.CompareRequest;
import com.tuan.debtwizard.features.planning.dto.CompareResponse;
import com.tuan.debtwizard.features.planning.dto.PlanComparisonDto;
import com.tuan.debtwizard.features.planning.dto.SavePlanRequest;
import com.tuan.debtwizard.features.planning.dto.SavedPlanResponse;
import com.tuan.debtwizard.features.planning.helper.SimulationHelper;
import com.tuan.debtwizard.features.planning.mapper.PlanEntityMapper;
import com.tuan.debtwizard.features.planning.mapper.SavedPlanMapper;
import com.tuan.debtwizard.features.planning.mapper.SnapshotMapper;
import com.tuan.debtwizard.features.planning.model.DebtSnapshot;
import com.tuan.debtwizard.features.planning.model.SavedPlan;
import com.tuan.debtwizard.features.planning.repository.SavedPlanRepository;
import com.tuan.debtwizard.features.user.model.User;
import com.tuan.debtwizard.features.user.repository.UserRepository;
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
    private final PlanEntityMapper planEntityMapper;
    private final SimulationHelper simulationHelper;

    public PlanningService(
            SimulationEngine simulationEngine,
            DebtRepository debtRepository,
            UserRepository userRepository,
            SavedPlanRepository savedPlanRepository,
            SnapshotMapper snapshotMapper,
            SavedPlanMapper savedPlanMapper,
            PlanEntityMapper planEntityMapper,
            SimulationHelper simulationHelper) {

        this.simulationEngine = simulationEngine;
        this.debtRepository = debtRepository;
        this.userRepository = userRepository;
        this.savedPlanRepository = savedPlanRepository;
        this.snapshotMapper = snapshotMapper;
        this.savedPlanMapper = savedPlanMapper;
        this.planEntityMapper = planEntityMapper;
        this.simulationHelper = simulationHelper;
    }

    @Transactional(readOnly = true)
    public CompareResponse comparePlans(
            CompareRequest request,
            UserDetails userDetails) {
        User user = getUser(userDetails.getUsername());
        validateStrategy(request);
        List<Debt> debts = loadAndVerifyDebts(
                request.getDebtIds(),
                user.getId());
        List<DebtSnapshot> snapshots = snapshotMapper.toSnapshots(debts);
        BigDecimal maxAllowed = simulationHelper.calculateMonthlyExtraBudget(user, snapshots);
        if (request.getMonthlyExtraPayment().compareTo(maxAllowed) > 0) {
            throw new AppException(ErrorCode.EXTRA_PAYMENT_EXCEEDS_BUDGET);
        }

        List<DebtSnapshot> firstPlanDebts = new ArrayList<>();
        List<DebtSnapshot> secondPlanDebts = new ArrayList<>();
        for (DebtSnapshot snapshot : snapshots) {
            firstPlanDebts.add(new DebtSnapshot(snapshot));
            secondPlanDebts.add(new DebtSnapshot(snapshot));
        }
        CompareResponse response = new CompareResponse();
        response.setMaxAllowedExtraPayment(maxAllowed);

        response.setFirstPlan(
                simulationEngine.simulate(
                        firstPlanDebts,
                        request.getFirstStrategy(),
                        request.getMonthlyExtraPayment()));

        response.setSecondPlan(
                simulationEngine.simulate(
                        secondPlanDebts,
                        request.getSecondStrategy(),
                        request.getMonthlyExtraPayment()));
        return response;
    }
    @Transactional
    public SavedPlanResponse savePlan(SavePlanRequest request, UserDetails userDetails) {
        User user = getUser(userDetails.getUsername());
        List<Debt> debts = loadAndVerifyDebts(request.getDebtIds(), user.getId());

        List<DebtSnapshot> snapshots = snapshotMapper.toSnapshots(debts);
        BigDecimal maxAllowed = simulationHelper.calculateMonthlyExtraBudget(user, snapshots);

        if (request.getMonthlyExtraPayment().compareTo(maxAllowed) > 0) {
            throw new AppException(ErrorCode.EXTRA_PAYMENT_EXCEEDS_BUDGET);
        }

        PlanComparisonDto result = simulationEngine.simulate(snapshots, request.getStrategy(), request.getMonthlyExtraPayment());

        savedPlanRepository.deleteByUserId(user.getId());
        savedPlanRepository.flush();
        SavedPlan plan = new SavedPlan();
        plan.setUser(user);
        plan.setStrategy(request.getStrategy());
        plan.setPlanName(result.getPlanName());
        plan.setMonthlyExtraPayment(request.getMonthlyExtraPayment());
        plan.setTotalInterestPaid(result.getTotalInterestPaid());
        plan.setPayoffDurationMonths(result.getPayoffDurationMonths());

        plan.setMonthlySchedules(planEntityMapper.toSchedules(plan, result.getSchedule(), debts));

        return savedPlanMapper.toResponse(savedPlanRepository.save(plan));
    }
    @Transactional(readOnly = true)
    public SavedPlanResponse getSavedPlan(UserDetails userDetails) {
        User user = getUser(userDetails.getUsername());
        SavedPlan plan = savedPlanRepository.findDetailByUserId(user.getId())
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

    private void validateStrategy(CompareRequest request) {
        if (request.getFirstStrategy() == null || request.getSecondStrategy() == null) {
            throw new AppException(ErrorCode.STRATEGY_MISSING);
        }
        if (request.getFirstStrategy().equals(request.getSecondStrategy())) {
            throw new AppException(ErrorCode.STRATEGY_DUPLICATE);
        }
    }

    @Transactional(readOnly = true)
    protected List<Debt> loadAndVerifyDebts(List<Long> debtIds, Long userId) {

        List<Debt> debts = debtRepository.findAllByIdWithUser(debtIds);
        if (debts.size() != debtIds.size()) {
            throw new AppException(ErrorCode.DEBT_NOT_FOUND);}
        for (Debt debt : debts) {
            if (!debt.getUser().getId().equals(userId)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);}
        }
        return debts;
    }
}