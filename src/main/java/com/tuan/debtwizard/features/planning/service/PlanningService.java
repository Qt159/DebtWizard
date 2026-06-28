package com.tuan.debtwizard.features.planning.service;

import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import com.tuan.debtwizard.features.planning.dto.CompareRequest;
import com.tuan.debtwizard.features.planning.dto.CompareResponse;
import com.tuan.debtwizard.features.planning.dto.PlanComparisonDto;
import com.tuan.debtwizard.features.planning.mapper.SnapshotMapper;
import com.tuan.debtwizard.features.planning.model.DebtSnapshot;
import com.tuan.debtwizard.features.planning.model.SimulationResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanningService {

    private final SimulationEngine simulationEngine;
    private final DebtRepository debtRepository;
    private final SnapshotMapper snapshotMapper;

    public PlanningService(SimulationEngine simulationEngine, DebtRepository debtRepository, SnapshotMapper snapshotMapper) {
        this.simulationEngine = simulationEngine;
        this.debtRepository = debtRepository;
        this.snapshotMapper = snapshotMapper;
    }

    public CompareResponse comparePlans(CompareRequest request) {
        List<Debt> debts = debtRepository.findAllById(request.getDebtIds());
        List<DebtSnapshot> snapshots = snapshotMapper.toSnapshots(debts);

        SimulationResult first = simulationEngine.simulate(snapshots, request.getFirstStrategy(),
                request.getMonthlyExtraPayment());

        SimulationResult second = simulationEngine.simulate(snapshots, request.getSecondStrategy(),
                request.getMonthlyExtraPayment());
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

        return dto;
    }
}