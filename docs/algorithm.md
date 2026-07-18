# Algorithm — DebtWizard

## 1. Chiến lược chọn khoản nợ ưu tiên

**MINIMIZE_INTEREST (Avalanche)**
- Chọn khoản nợ có `interestRate` cao nhất trong số các khoản nợ còn active.
- Mục tiêu: giảm tổng lãi phải trả.

**IMPROVE_CASHFLOW**
- Với mỗi khoản nợ active, tính:
  - `estimatedPayoffMonths = balance / minimumPayment`
  - `priorityScore = minimumPayment / estimatedPayoffMonths`
- Chọn khoản nợ có `priorityScore` **cao nhất**.
- **Giải thích:** Score = `minimumPayment / (balance/minimum)` — ưu tiên các khoản nợ vừa có **minimum payment lớn** (giải phóng nhiều cashflow khi tất toán), vừa **gần trả hết** (tất toán sớm). Sau khi tất toán, minimum payment của khoản đó được cộng vào `snowballBonus` cho các tháng tiếp theo.
- Mục tiêu: tối đa hóa dòng tiền được giải phóng trong thời gian ngắn nhất.

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
   - Phần trả lãi: `interestPortion = min(currentInterestPaid, payment)` → ghi đè `currentInterestPaid`
   - Phần trả gốc: `principalPortion = payment - interestPortion` → cộng vào `currentPrincipalPaid`
   - Trừ `payment` khỏi `balance`, lưu `payment` vào `currentMinimumPaid`.
5. **Chọn target debt** theo `RepaymentStrategy`.
6. **Phân bổ extra payment** vào target:
   `totalExtraThisMonth = monthlyExtraPayment (gốc từ user) + snowballBonus (tích lũy)`
   `extraUsed = min(totalExtraThisMonth, target.balance)`
   Trừ `extraUsed` khỏi `target.balance`.
   > `monthlyExtraPayment` gốc **không thay đổi** qua các tháng.
7. **Cashflow release:** với mỗi debt vừa `balance ≤ 0`:
   `paidOff = true`
   `snowballBonus += minimumPayment` của khoản đó (tích lũy vĩnh viễn từ tháng tiếp theo)
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
