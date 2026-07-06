# Algorithm — DebtWizard

## 1. Chiến lược chọn khoản nợ ưu tiên

**MINIMIZE_INTEREST (Avalanche)**
- Chọn khoản nợ có `interestRate` cao nhất trong số các khoản nợ còn active.
- Mục tiêu: giảm tổng lãi phải trả.

**IMPROVE_CASHFLOW**
- Với mỗi khoản nợ active, tính:
  - `totalMonthlyPayment = minimumPayment + monthlyExtraPayment`
  - `estimatedPayoffMonths = balance / totalMonthlyPayment`
  - `priorityScore = totalMonthlyPayment / estimatedPayoffMonths`
  **Giải thích:**
Chiến lược này ưu tiên tất toán những khoản nợ có khả năng **giải phóng được khoản thanh toán tối thiểu (minimum payment) lớn trong thời gian ngắn**. Công thức trên cân bằng giữa hai yếu tố:
- `totalMonthlyPayment` càng lớn → sau khi tất toán sẽ giải phóng được nhiều dòng tiền hàng tháng.
- `estimatedPayoffMonths` càng nhỏ → khoản nợ có thể được tất toán sớm.
- Chọn khoản nợ có `priorityScore` cao nhất.
- Mục tiêu: tất toán nhanh các khoản nợ giải phóng nhiều cashflow nhất.
- Giải thích: `priorityScore = totalMonthlyPayment / estimatedPayoffMonths` sẽ ưu tiên các khoản nợ vừa mang lại **cashflow release lớn**, vừa **có thể hoàn thành nhanh**, giúp số tiền trả thêm (`monthlyExtraPayment`) tăng dần theo cơ chế Cashflow Release ở các tháng tiếp theo.

## 2. Simulation

**Input:** `List<DebtSnapshot>`, `monthlyExtraPayment`, `RepaymentStrategy`

**Output:** `PlanComparisonDto` — bao gồm schedule chi tiết từng tháng, tổng lãi, số tháng trả hết

**Steps (mỗi tháng):**
1. Lấy danh sách `activeDebts` — các khoản nợ có `balance > 0`.
2. Reset monthly tracking cho từng debt (`minimumPaid`, `extraPaid`, `principalPaid`, `interestPaid` về 0).
3. **Tính lãi trước:** với mỗi active debt:
   `interest = balance × (interestRate / 100 / 12)`
   Cộng `interest` vào `balance`, lưu vào `currentInterestPaid`.
4. **Thanh toán minimum payment:** với mỗi active debt:
   `payment = min(minimumPayment, balance)`
   Trừ `payment` khỏi `balance`, lưu vào `currentMinimumPaid` và `currentPrincipalPaid`.
5. **Chọn target debt** theo `RepaymentStrategy`.
6. **Phân bổ extra payment** vào target:
   `extraUsed = min(monthlyExtraPayment, target.balance)`
   Trừ `extraUsed` khỏi `target.balance`.
   `monthlyExtraPayment -= extraUsed`
7. **Cashflow release:** với mỗi debt vừa `balance ≤ 0`:
   `paidOff = true`
   `monthlyExtraPayment += minimumPayment` của khoản đó
8. Ghi `SimulationMonthDto` với toàn bộ per-debt breakdown.
9. Lặp lại cho đến khi không còn active debt hoặc `monthIndex > 600`.


## 3. Interest Accrual (Scheduler hàng ngày)

Khác với simulation (tính theo tháng), accrual thực tế tính theo ngày:

```
interest = totalPrincipal × (interestRate / 100 / 365) × numberOfDays   [FLAT]
interest = remainingPrincipal × (interestRate / 100 / 365) × numberOfDays [REDUCING_BALANCE]
```

- numberOfDays = số ngày từ lastInterestAccruedDate đến ngày hiện tại.
- Sau khi accrual: cập nhật accruedInterest và lastInterestAccruedDate.


## 4. Quick Win Detection *(Dự kiến phát triển)*

**Input:** Repayment Schedule từ Simulation

**Output:** `QuickWinResponse { opportunities, bestOpportunity }`

**Steps:**
1. Duyệt từng tháng trong Repayment Schedule.
2. Với mỗi khoản nợ còn active:
   - `additionalPaymentRequired` = phần tiền thêm cần để tất toán khoản nợ đó trong tháng này
   - `monthlyPaymentReleased` = `minimumPayment` của khoản nợ đó nếu được tất toán
   - `expectedDurationReduction` = số tháng rút ngắn được nếu tất toán sớm
   - `quickWinScore = monthlyPaymentReleased × expectedDurationReduction / additionalPaymentRequired`
3. Sắp xếp theo `quickWinScore` giảm dần.
4. Trả về danh sách và `bestOpportunity` (score cao nhất).
