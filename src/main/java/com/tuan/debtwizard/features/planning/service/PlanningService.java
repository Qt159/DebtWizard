package com.tuan.debtwizard.features.planning.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import com.tuan.debtwizard.features.planning.dto.CompareRequest;
import com.tuan.debtwizard.features.planning.dto.CompareResponse;
import com.tuan.debtwizard.features.planning.dto.PlanComparisonDto;
import com.tuan.debtwizard.features.planning.mapper.RepaymentMapper;
import com.tuan.debtwizard.features.planning.mapper.SnapshotMapper;
import com.tuan.debtwizard.features.planning.model.DebtSnapshot;
import com.tuan.debtwizard.features.planning.model.SimulationResult;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlanningService {

    private final SimulationEngine simulationEngine;
    private final DebtRepository debtRepository;
    private final SnapshotMapper snapshotMapper;
    private final RepaymentMapper repaymentMapper;

    public PlanningService(SimulationEngine simulationEngine,
                           DebtRepository debtRepository,
                           SnapshotMapper snapshotMapper,
                           RepaymentMapper repaymentMapper) {
        this.simulationEngine = simulationEngine;
        this.debtRepository = debtRepository;
        this.snapshotMapper = snapshotMapper;
        this.repaymentMapper = repaymentMapper;
    }

    public CompareResponse comparePlans(CompareRequest request, UserDetails userDetails) {
        List<Debt> debts = debtRepository.findAllById(request.getDebtIds());
        for (Debt debt : debts) {
            if (!debt.getUser().getUsername().equals(userDetails.getUsername())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }

        List<DebtSnapshot> snapshots = snapshotMapper.toSnapshots(debts);

        List<DebtSnapshot> snapshotsForFirst = new ArrayList<>();
        for (DebtSnapshot s : snapshots) {
            snapshotsForFirst.add(new DebtSnapshot(s));
        }

        List<DebtSnapshot> snapshotsForSecond = new ArrayList<>();
        for (DebtSnapshot s : snapshots) {
            snapshotsForSecond.add(new DebtSnapshot(s));
        }

        SimulationResult first = simulationEngine.simulate(
                snapshotsForFirst, request.getFirstStrategy(), request.getMonthlyExtraPayment());

        SimulationResult second = simulationEngine.simulate(
                snapshotsForSecond, request.getSecondStrategy(), request.getMonthlyExtraPayment());

        CompareResponse response = new CompareResponse();
        response.setFirstPlan(map(first));
        response.setSecondPlan(map(second));
        return response;
    }

    private PlanComparisonDto map(SimulationResult result) {
        PlanComparisonDto dto = new PlanComparisonDto();
        dto.setStrategy(result.getStrategy());
        dto.setPlanName(result.getStrategy().getDisplayName());
        dto.setTotalInterestPaid(result.getTotalInterestPaid());
        dto.setPayoffDurationMonths(result.getPayoffDurationMonths());
        dto.setSchedule(repaymentMapper.toSimulationMonthDtos(result.getMonths()));
        return dto;
    }
}
