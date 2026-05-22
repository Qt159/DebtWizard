package com.tuan.debtwizard.features.debt.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.auth.model.User;
import com.tuan.debtwizard.features.auth.repository.UserRepository;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.mapper.DebtMapper;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.dto.DebtListItem;
import com.tuan.debtwizard.features.debt.dto.DebtRequest;
import com.tuan.debtwizard.features.debt.dto.DebtResponse;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class DebtService {

    private final DebtRepository debtRepository;
    private final DebtMapper debtMapper;
    private final UserRepository userRepository;
    public DebtService(
            DebtRepository debtRepository,
            DebtMapper debtMapper,
            UserRepository userRepository
    ) {
        this.debtRepository = debtRepository;
        this.debtMapper = debtMapper;
        this.userRepository = userRepository;
    }
    private User findUserOrThrow(UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
    // CREATE
    public DebtResponse createDebt(DebtRequest req, UserDetails userDetails) {
        User currentUser = findUserOrThrow(userDetails);
        Debt debt = debtMapper.toEntity(req);
        debt.setUser(currentUser);
        debt.setStatus(DebtStatus.ACTIVE);
        Debt savedDebt = debtRepository.save(debt);
        return debtMapper.toResponse(savedDebt);
    }
    // GET LIST
    @Transactional(readOnly = true)
    public List<DebtListItem> getDebts(
            UserDetails userDetails,
            DebtStatus status
    ) {
        User currentUser = findUserOrThrow(userDetails);
        List<Debt> debts;
        if (status == null) {
            debts = debtRepository.findByUserIdAndDeletedFalse(currentUser.getId());
        } else {
            debts = debtRepository.findByUserIdAndStatusAndDeletedFalse(
                    currentUser.getId(),
                    status
            );
        }
        List<DebtListItem> items = new ArrayList<>();
        for (Debt debt : debts) {
            items.add(debtMapper.toListItem(debt));
        }
        return items;
    }
    // GET DETAIL
    @Transactional(readOnly = true)
    public DebtResponse getDebtById(
            Long id,
            UserDetails userDetails
    ) {
        User currentUser = findUserOrThrow(userDetails);
        Debt debt = debtRepository
                .findByIdAndUserIdAndDeletedFalse(id, currentUser.getId())
                .orElseThrow(() ->
                        new AppException(ErrorCode.DEBT_NOT_FOUND)
                );
        return debtMapper.toResponse(debt);
    }
    //UPDATE
    public DebtResponse updateDebt(Long id, DebtRequest req, UserDetails userDetails) {
        User currentUser = findUserOrThrow(userDetails);
        Debt debt = debtRepository
                .findByIdAndUserIdAndDeletedFalse(id, currentUser.getId())
                .orElseThrow(() ->
                        new AppException(ErrorCode.DEBT_NOT_FOUND)
                );
        debt.setLenderName(req.getLenderName());
        debt.setInterestRate(req.getInterestRate());
        debt.setMonthlyPayment(req.getMonthlyPayment());
        debt.setDueDay(req.getDueDay());
        debt.setDebtType(req.getDebtType());
        Debt updatedDebt = debtRepository.save(debt);
        return debtMapper.toResponse(updatedDebt);
    }
    //DELETE
    public void deleteDebt(Long id, UserDetails userDetails) {
        User currentUser = findUserOrThrow(userDetails);
        Debt debt = debtRepository.findByIdAndUserIdAndDeletedFalse(id, currentUser.getId())
                .orElseThrow(() ->
                        new AppException(ErrorCode.DEBT_NOT_FOUND));
        debt.setDeleted(true);
        debtRepository.save(debt);
    }
    }
