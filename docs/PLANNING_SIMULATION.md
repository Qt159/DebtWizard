# Planning & Simulation Engine — DebtWizard

---

## 1. Tổng quan Động cơ Mô phỏng (Simulation Engine Overview)

Động cơ Mô phỏng (**Simulation Engine**) là thành phần cốt lõi mang lại giá trị ra quyết định tài chính của nền tảng **DebtWizard**. 

Khác với các ứng dụng ghi chép thu chi đơn thuần, Simulation Engine cho phép người dùng chạy các kịch bản "What-If" giả định trong tương lai:
- *"Nếu tôi dành thêm 2.000.000 VNĐ mỗi tháng, sau bao lâu tôi sẽ sạch nợ?"*
- *"Tôi nên ưu tiên dồn tiền trả khoản vay ngân hàng lãi suất cao hay khoản vay tín chấp kỳ hạn ngắn?"*
- *"Tổng số tiền lãi tôi tiết kiệm được giữa 2 chiến lược là bao nhiêu?"*

```text
  [Danh sách nợ (Debts)] ──┐
  [Thu nhập & Chi phí]   ──┼─► [SimulationEngine] ─► [Bảng so sánh 2 kế hoạch (Stateless)]
  [Extra Payment tự chọn]──┘           │                               │
                                       ▼                               ▼
                          [In-Memory DebtSnapshot]        [Lưu kế hoạch đã chọn (DB)]
```

---

## 2. Mô hình Dữ liệu In-Memory (`DebtSnapshot`)

Để đảm bảo hiệu năng cao và tính độc lập tuyệt đối giữa các chiến lược mà không làm thay đổi dữ liệu thực trong cơ sở dữ liệu, quá trình mô phỏng sử dụng đối tượng **`DebtSnapshot`** chạy hoàn toàn trong bộ nhớ RAM:

| Thuộc tính | Kiểu dữ liệu | Ý nghĩa trong Simulation |
|---|---|---|
| `debtId` | `Long` | ID của khoản nợ gốc |
| `lenderName` | `String` | Tên bên cho vay / ngân hàng |
| `balance` | `BigDecimal` | Dư nợ hiện tại trong tháng mô phỏng (`remainingPrincipal + accruedInterest`) |
| `interestRate` | `BigDecimal` | Lãi suất năm (%) |
| `minimumPayment`| `BigDecimal` | Số tiền thanh toán tối thiểu hàng tháng (`expectedMonthlyPayment`) |
| `paidOff` | `boolean` | Cờ đánh dấu khoản nợ đã hoàn tất trả hết |
| `currentMinimumPaid` | `BigDecimal` | Số tiền trả tối thiểu thực tế trong tháng |
| `currentExtraPaid` | `BigDecimal` | Số tiền trả thêm (extra) được dồn vào trong tháng |
| `currentPrincipalPaid` | `BigDecimal`| Tổng số tiền trả vào gốc trong tháng |
| `currentInterestPaid` | `BigDecimal` | Số tiền trả vào lãi phát sinh trong tháng |

---

## 3. Xác thực & Tính toán Ngân sách Trả thêm (Extra Budgeting)

Trước khi khởi chạy mô phỏng, hệ thống tự động xác định ngưỡng tối đa người dùng có thể chi trả thêm (`maxAllowedExtraPayment`) để ngăn ngừa lập kế hoạch vượt quá khả năng tài chính:

### Công thức tính Ngân sách Khả dụng:
$$\text{maxAllowedExtraPayment} = \max\Big(0,\; \text{monthlyIncome} - \text{monthlyEssentialExpenses} - \sum \text{minimumPayment}\Big)$$

- **Quy tắc BR02:** Nếu người dùng nhập `monthlyExtraPayment > maxAllowedExtraPayment`, hệ thống từ chối mô phỏng và trả mã lỗi `EXTRA_PAYMENT_EXCEEDS_BUDGET`.
- Giá trị `maxAllowedExtraPayment` được trả về trong API `/api/planning/compare` để giao diện người dùng hiển thị thanh trượt hoặc gợi ý ngân sách trực quan.

---

## 4. Các Chiến lược Trả nợ (Repayment Strategies)

Simulation Engine hiện hỗ trợ 2 chiến lược tối ưu hóa theo 2 mục tiêu tài chính khác nhau:

### 4.1 Chiến lược 1: `MINIMIZE_INTEREST` (Phương pháp Debt Avalanche)
- **Mục tiêu:** Tiết kiệm tối đa tổng chi phí tiền lãi phải trả cho các bên cho vay.
- **Tiêu chí lựa chọn (`MinimizeInterestStrategy`):**
  - Tại mỗi tháng, trong số các khoản nợ còn dư nợ (`balance > 0`), hệ thống chọn khoản nợ có **lãi suất năm (`interestRate`) cao nhất**.
  - Toàn bộ ngân sách trả thêm (`totalExtraThisMonth`) được dồn vào khoản nợ này cho đến khi tất toán.
- **Ưu điểm:** Tối ưu hóa tuyệt đối về mặt toán học và tài chính dài hạn.

---

### 4.2 Chiến lược 2: `IMPROVE_CASHFLOW` (Phương pháp Tối đa hóa Dòng tiền Tự do)
- **Mục tiêu:** Nhanh chóng tất toán các khoản nợ phù hợp để xóa bỏ nghĩa vụ trả nợ tối thiểu hàng tháng, từ đó giải phóng dòng tiền tự do trong thời gian ngắn nhất.
- **Tiêu chí lựa chọn (`ImproveCashflowStrategy`):**
  - Với mỗi khoản nợ còn hoạt động, tính thời gian dự kiến trả hết theo mức tối thiểu:
    $$\text{estimatedPayoffMonths} = \frac{\text{balance}}{\text{minimumPayment}}$$
  - Tính điểm ưu tiên giải phóng dòng tiền (**Priority Score**):
    $$\text{score} = \frac{\text{minimumPayment}}{\text{estimatedPayoffMonths}} = \frac{\text{minimumPayment}^2}{\text{balance}}$$
  - Hệ thống chọn khoản nợ có **`score` cao nhất** làm mục tiêu dồn Extra Payment.
- **Phân tích cơ chế:**
  - Khoản nợ có `minimumPayment` lớn sẽ giải phóng được một lượng tiền mặt lớn hàng tháng sau khi dứt điểm.
  - Khoản nợ có `balance` nhỏ sẽ cần ít thời gian và tiền bạc nhất để dứt điểm.
  - Tỷ số $\frac{\text{minimumPayment}^2}{\text{balance}}$ cân bằng hoàn hảo 2 yếu tố trên: ưu tiên khoản nợ vừa giải phóng nhiều tiền vừa nhanh về đích.

---

## 5. Vòng lặp Mô phỏng Hàng tháng (Simulation Lifecycle)

Mỗi chiến lược được chạy độc lập thông qua phương thức `SimulationEngine.simulate()`:

```text
Khởi tạo: monthIndex = 0, totalInterest = 0, snowballBonus = 0
                      │
                      ▼
   ┌───────────────────────────────────────┐
   │ Có khoản nợ nào còn balance > 0?     │◄───────────────────┐
   └──────────────────┬────────────────────┘                    │
                      │ YES                                     │
                      ▼                                         │
        1. Tăng monthIndex (+1)                                 │
           (Nếu monthIndex > 600 -> Lỗi SIMULATION_FAILED)      │
                      │                                         │
                      ▼                                         │
        2. Reset tracking tháng của các khoản nợ active         │
                      │                                         │
                      ▼                                         │
        3. Tính lãi phát sinh trong tháng                       │
           interest = balance × (interestRate / 100 / 12)       │
           balance += interest, totalInterest += interest       │
                      │                                         │
                      ▼                                         │
        4. Thanh toán Minimum Payment cho TẤT CẢ các nợ         │
           payment = min(minimumPayment, balance)               │
           - Trừ hết lãi phát sinh trước (interest-first)       │
           - Phần còn lại giảm nợ gốc                           │
           - balance -= payment                                 │
                      │                                         │
                      ▼                                         │
        5. Xác định Target Debt theo Chiến lược                 │
           (Avalanche hoặc Improve Cashflow)                    │
                      │                                         │
                      ▼                                         │
        6. Phân bổ Extra Payment vào Target Debt                │
           totalExtra = monthlyExtraPayment + snowballBonus     │
           extraUsed = min(totalExtra, target.balance)          │
           target.balance -= extraUsed                          │
                      │                                         │
                      ▼                                         │
        7. Giải phóng Dòng tiền (Cashflow Release / Snowball)   │
           Với các nợ vừa balance <= 0:                         │
           - paidOff = true                                     │
           - snowballBonus += debt.minimumPayment               │
                      │                                         │
                      ▼                                         │
        8. Đóng gói dữ liệu tháng vào SimulationMonthDto        │
                      │                                         │
                      └─────────────────────────────────────────┘
                      │ NO (Tất cả nợ đã tất toán)
                      ▼
        Tổng kết kết quả: totalInterest, monthIndex (thời gian sạch nợ),
        Lịch trình chi tiết -> Trả về PlanComparisonDto
```

---

## 6. Tính toán Lãi suất & Thanh toán Hàng tháng (Interest Engine)

Hệ thống hỗ trợ 2 phương pháp tính lãi chuẩn ngân hàng thông qua `InterestCalculationStrategy`:

### 6.1 Lãi suất Cố định (Flat Interest)
Lãi được tính đều trên nợ gốc ban đầu trong suốt kỳ hạn vay.

1. **Số tiền trả hàng tháng cố định khi tạo nợ:**
   $$M = \frac{P}{n} + P \times \left(\frac{r}{100 \times 12}\right)$$
   *(Trong đó: $P$ = nợ gốc ban đầu, $n$ = kỳ hạn tháng, $r$ = lãi suất năm %)*

2. **Lãi phát sinh cộng dồn hàng ngày (Daily Accrual):**
   $$I_{\text{daily}} = P \times \left(\frac{r}{100 \times 365}\right) \times \text{numberOfDays}$$

---

### 6.2 Lãi suất Dư nợ Giảm dần (Reducing Balance / Amortization)
Lãi được tính dựa trên nợ gốc thực tế còn lại tại thời điểm tính toán.

1. **Số tiền trả hàng tháng theo chuẩn Amortization:**
   $$M = P \times \frac{r_m (1 + r_m)^n}{(1 + r_m)^n - 1}$$
   *(Trong đó: $r_m = \frac{r}{100 \times 12}$ là lãi suất tháng)*

2. **Lãi phát sinh cộng dồn hàng ngày (Daily Accrual):**
   $$I_{\text{daily}} = P_{\text{remaining}} \times \left(\frac{r}{100 \times 365}\right) \times \text{numberOfDays}$$

---

## 7. Cơ chế Lưu trữ Kế hoạch đã chọn (Plan Persistence)

Khi người dùng quyết định lựa chọn một kế hoạch sau khi xem so sánh (`POST /api/planning/save`):
1. Hệ thống tái chạy mô phỏng chính xác với chiến lược và `monthlyExtraPayment` đã chọn.
2. Xóa kế hoạch cũ của người dùng (Hard Delete cascade `saved_plans` -> `plan_monthly_schedules` -> `plan_debt_payments`).
3. Lưu bản ghi mới vào cơ sở dữ liệu:
   - `saved_plans`: Lưu tổng quan chiến lược, tổng lãi dự kiến và số tháng hoàn thành.
   - `plan_monthly_schedules`: Lưu lịch trình chi tiết từng tháng.
   - `plan_debt_payments`: Lưu chi tiết phân bổ cho từng khoản nợ trong mỗi tháng (sao chép tĩnh `debt_name` để đảm bảo tính bất biến khi hiển thị lại).

---

## 8. Thuật toán Phát hiện Cơ hội Tất toán Sớm (Quick Win Detection) *(Định hướng nâng cấp)*

Mục tiêu: Quét lịch trình thanh toán dự kiến để chỉ ra cho người dùng những thời điểm mà chỉ cần trả thêm một khoản tiền nhỏ là có thể dứt điểm ngay một khoản nợ và giải phóng khoản trả hàng tháng.

### Thuật toán đánh giá:
1. Duyệt qua từng tháng $m$ trong lịch trình mô phỏng.
2. Với mỗi khoản nợ còn hoạt động:
   - $\text{additionalPaymentRequired} = \text{balance}$ còn lại của nợ đó tại tháng $m$.
   - $\text{monthlyPaymentReleased} = \text{minimumPayment}$ của khoản nợ đó.
   - $\text{expectedDurationReduction} = \text{Số tháng rút ngắn được}$.
   - $\text{quickWinScore} = \frac{\text{monthlyPaymentReleased} \times \text{expectedDurationReduction}}{\text{additionalPaymentRequired}}$.
3. Xếp hạng và trả về cơ hội có `quickWinScore` cao nhất cho người dùng.
