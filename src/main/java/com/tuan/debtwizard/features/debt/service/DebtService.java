package com.tuan.debtwizard.features.debt.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.debt.dto.CreateDebtRequest;
import com.tuan.debtwizard.features.debt.dto.DebtListItemResponse;
import com.tuan.debtwizard.features.debt.dto.DebtResponse;
import com.tuan.debtwizard.features.debt.dto.UpdateDebtRequest;
import com.tuan.debtwizard.features.debt.mapper.DebtMapper;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.model.InterestCalculationMethod;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import com.tuan.debtwizard.features.debt.service.interest.InterestCalculationService;
import com.tuan.debtwizard.features.user.model.User;
import com.tuan.debtwizard.features.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Service
public class DebtService {

    private final DebtRepository debtRepository;
    private final DebtMapper debtMapper;
    private final UserRepository userRepository;
    private final DebtStateService debtStateService;
    private final InterestCalculationService interestCalculationService;

    public DebtService(DebtRepository debtRepository,
                       DebtMapper debtMapper,
                       UserRepository userRepository,
                       DebtStateService debtStateService,
                       InterestCalculationService interestCalculationService) {
        this.debtRepository = debtRepository;
        this.debtMapper = debtMapper;
        this.userRepository = userRepository;
        this.debtStateService = debtStateService;
        this.interestCalculationService = interestCalculationService;
    }

    private User findUserOrThrow(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public DebtResponse createDebt(CreateDebtRequest request, UserDetails userDetails) {
        User currentUser = findUserOrThrow(userDetails);
        Debt debt = debtMapper.toEntity(request, request.getInterestSettings());
        debt.setUser(currentUser);
        debt.setStatus(DebtStatus.ACTIVE);
        debt.setNextDueDate(debtStateService.calculateFirstDueDate(debt));
        debt.setExpectedMonthlyPayment(interestCalculationService.calculateMonthlyPayment(debt));
        return debtMapper.toResponse(debtRepository.save(debt));
    }

    @Transactional(readOnly = true)
    public List<DebtListItemResponse> getDebts(
            UserDetails userDetails,
            String search,
            DebtStatus status,
            InterestCalculationMethod interestMethod,
            String sortBy,
            String sortDir) {

        User currentUser = findUserOrThrow(userDetails);

        List<Debt> debts = debtRepository.findByUserIdAndDeletedFalse(currentUser.getId());
        if(search != null && !search.isBlank()) {
            String keyword = search.toLowerCase();
            debts.removeIf(debt ->!debt.getLenderName().toLowerCase().contains(keyword));
        }
        // filter
        if(status != null) {
            debts.removeIf(debt->debt.getStatus() != status);
        }
        if(interestMethod != null) {
            debts.removeIf(debt -> debt.getInterestSettings()
                            .getInterestCalculationMethod() != interestMethod);
        }
        Comparator<Debt> comparator;
        switch (sortBy) {
            case "createdAt":
                comparator = Comparator.comparing(Debt::getCreatedAt);
                break;
            case "remainingPrincipal":
                comparator = Comparator.comparing(Debt::getRemainingPrincipal);
                break;
            default: throw new IllegalArgumentException("Invalid sortBy. Allowed values: createdAt, remainingPrincipal");
        }

        if("desc".equalsIgnoreCase(sortDir)) {
            comparator = comparator.reversed();
        }
        else if(!"asc".equalsIgnoreCase(sortDir)) {
            throw new IllegalArgumentException("Invalid sortDir. Allowed values: asc, desc");
        }
        debts.sort(comparator);
        List<DebtListItemResponse> responses = new ArrayList<>();
        for (Debt debt : debts) {
            responses.add(debtMapper.toListItem(debt));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public DebtResponse getDebtById(Long id, UserDetails userDetails) {
        User currentUser = findUserOrThrow(userDetails);
        Debt debt = debtRepository.findByIdAndUserIdAndDeletedFalse(id, currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DEBT_NOT_FOUND));
        return debtMapper.toResponse(debt);
    }

    @Transactional
    public DebtResponse updateDebt(Long id, UpdateDebtRequest request, UserDetails userDetails) {
        User currentUser = findUserOrThrow(userDetails);
        Debt debt = debtRepository.findByIdAndUserIdAndDeletedFalse(id, currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DEBT_NOT_FOUND));
        if (request.getLenderName() != null) {
            debt.setLenderName(request.getLenderName());
        }
        return debtMapper.toResponse(debtRepository.save(debt));
    }

    @Transactional
    public void deleteDebt(Long id, UserDetails userDetails) {
        User currentUser = findUserOrThrow(userDetails);
        Debt debt = debtRepository.findByIdAndUserIdAndDeletedFalse(id, currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DEBT_NOT_FOUND));
        debt.setDeleted(true);
        debtRepository.save(debt);
    }
}
