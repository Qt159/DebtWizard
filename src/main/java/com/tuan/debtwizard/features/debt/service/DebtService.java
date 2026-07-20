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
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class DebtService {

    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("createdAt", "remainingPrincipal");

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
        Sort sort = buildSort(sortBy, sortDir, ALLOWED_SORT_FIELDS, "createdAt");

        List<Debt> debts = debtRepository.findWithFilters(
                currentUser.getId(), search, status, interestMethod, sort);

        List<DebtListItemResponse> result = new ArrayList<>();
        for (Debt debt : debts) {
            result.add(debtMapper.toListItem(debt));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public DebtResponse getDebtById(Long id, UserDetails userDetails) {
        User currentUser = findUserOrThrow(userDetails);
        Debt debt = debtRepository.findByIdAndUserIdAndDeletedFalse(id, currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DEBT_NOT_FOUND));
        return debtMapper.toResponse(debt);
    }

    public DebtResponse updateDebt(Long id, UpdateDebtRequest request, UserDetails userDetails) {
        User currentUser = findUserOrThrow(userDetails);
        Debt debt = debtRepository.findByIdAndUserIdAndDeletedFalse(id, currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DEBT_NOT_FOUND));
        if (request.getLenderName() != null) {
            debt.setLenderName(request.getLenderName());
        }
        return debtMapper.toResponse(debtRepository.save(debt));
    }

    public void deleteDebt(Long id, UserDetails userDetails) {
        User currentUser = findUserOrThrow(userDetails);
        Debt debt = debtRepository.findByIdAndUserIdAndDeletedFalse(id, currentUser.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DEBT_NOT_FOUND));
        debt.setDeleted(true);
        debtRepository.save(debt);
    }

    public static Sort buildSort(String sortBy, String sortDir, Set<String> allowed, String defaultField) {
        String field = allowed.contains(sortBy) ? sortBy : defaultField;
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, field);
    }
}
