package com.tuan.debtwizard.features.payment.service;

import com.tuan.debtwizard.exception.AppException;
import com.tuan.debtwizard.exception.ErrorCode;
import com.tuan.debtwizard.features.auth.model.User;
import com.tuan.debtwizard.features.auth.repository.UserRepository;
import com.tuan.debtwizard.features.debt.model.Debt;
import com.tuan.debtwizard.features.debt.model.DebtStatus;
import com.tuan.debtwizard.features.debt.repository.DebtRepository;
import com.tuan.debtwizard.features.debt.service.DebtStateService;
import com.tuan.debtwizard.features.interest.service.InterestAccrualService;
import com.tuan.debtwizard.features.payment.dto.PaymentAllocationResult;
import com.tuan.debtwizard.features.payment.dto.PaymentListItem;
import com.tuan.debtwizard.features.payment.dto.PaymentRequest;
import com.tuan.debtwizard.features.payment.dto.PaymentResponse;
import com.tuan.debtwizard.features.payment.mapper.PaymentMapper;
import com.tuan.debtwizard.features.payment.model.Payment;
import com.tuan.debtwizard.features.payment.repository.PaymentRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DebtRepository debtRepository;
    private final PaymentMapper paymentMapper;
    private final UserRepository userRepository;
    private final DebtStateService debtStateService;
    private final PaymentAllocationService paymentAllocationService;
    private final InterestAccrualService interestAccrualService;

    public PaymentService(
            PaymentRepository paymentRepository,
            DebtRepository debtRepository,
            PaymentMapper paymentMapper,
            UserRepository userRepository,
            DebtStateService debtStateService,
            PaymentAllocationService paymentAllocationService,
            InterestAccrualService interestAccrualService) {

        this.paymentRepository = paymentRepository;
        this.debtRepository = debtRepository;
        this.paymentMapper = paymentMapper;
        this.userRepository = userRepository;
        this.debtStateService = debtStateService;
        this.paymentAllocationService = paymentAllocationService;
        this.interestAccrualService = interestAccrualService;
    }

    @Transactional
    public PaymentResponse createPayment(
            PaymentRequest request,
            UserDetails userDetails) {
        User user = getUserByUsername(userDetails.getUsername());
        Debt debt = debtRepository
                .findByIdAndUserIdAndDeletedFalse(request.getDebtId(), user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DEBT_NOT_FOUND));

        if (debt.getStatus() == DebtStatus.PAID_OFF) {
            throw new AppException(ErrorCode.DEBT_ALREADY_PAID_OFF);
        }

        validatePaymentDate(request.getPaymentDate(), debt);

        // Cập nhật lãi tới hiện tại
        interestAccrualService.accrueInterest(debt,request.getPaymentDate());

        // Phân bổ tiền
        PaymentAllocationResult allocation =
                paymentAllocationService.allocate(debt, request.getAmount());

        // Cập nhật thông tin khoản vay
        debt.setLastPaymentDate(request.getPaymentDate());

        debtStateService.moveNextDueDate(debt, request.getAmount());
        debtStateService.refreshDebtStatus(debt);

        if (debt.getStatus() == DebtStatus.PAID_OFF) {
            debt.setPaidOffAt(LocalDateTime.now());
        }
        debtRepository.save(debt);
        Payment payment =
                paymentMapper.toEntity(request, debt, allocation);

        Payment saved = paymentRepository.save(payment);
        return paymentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(
            UserDetails userDetails,
            Long id) {
        User user = getUserByUsername(userDetails.getUsername());
        Payment payment = paymentRepository
                .findByIdAndDebtUserId(id, user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));

        return paymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentListItem> getPayments(
            UserDetails userDetails,
            Long debtId) {
        User user = getUserByUsername(userDetails.getUsername());
        debtRepository.findByIdAndUserIdAndDeletedFalse(
                        debtId,
                        user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DEBT_NOT_FOUND));
        List<Payment> payments = paymentRepository.findByDebtId(debtId);
        List<PaymentListItem> items = new ArrayList<>();
        for (Payment payment : payments) {
            items.add(paymentMapper.toListItem(payment));
        }
        return items;
    }
    private void validatePaymentDate(LocalDate paymentDate, Debt debt) {
        /*Không cho thanh toán ở tương lai.
        Không cho thanh toán trước khi khoản vay tồn tại.
        Không cho thanh toán lùi thời gian.
         */
        if (paymentDate.isAfter(LocalDate.now())
                || paymentDate.isBefore(debt.getStartDate())
                || (debt.getLastPaymentDate() != null
                && paymentDate.isBefore(debt.getLastPaymentDate()))) {
            throw new AppException(ErrorCode.INVALID_PAYMENT_DATE);
        }
    }
    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}