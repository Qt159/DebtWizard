# Software Architecture Document (SAD) — DebtWizard

## 1. Giới thiệu

### 1.1 Mục tiêu hệ thống

**Problem**

Nhiều người đang mắc nhiều khoản nợ cùng lúc nhưng không biết:
- Nên ưu tiên trả khoản nào trước
- Nếu có thêm tiền dư thì nên phân bổ như thế nào
- Kế hoạch nào giúp tiết kiệm lãi nhiều nhất hoặc giảm áp lực dòng tiền
- Khi nào có thể thoát nợ
- Sức khỏe tài chính hiện tại đang ở mức nào

**System Goal**
- Quản lý các khoản nợ cá nhân và lịch sử thanh toán
- Tính lãi tự động và theo dõi dư nợ còn lại
- Phân tích sức khỏe tài chính qua 4 chỉ số định lượng
- Mô phỏng, so sánh 2 chiến lược trả nợ và lưu kế hoạch đã chọn
- Hỗ trợ người dùng đưa ra quyết định trả nợ phù hợp với mục tiêu tài chính

### 1.2 Phạm vi hệ thống
- Quản lý khoản nợ cá nhân: `BANKING`, `PERSONAL_LOAN`, `CREDIT`
- Hỗ trợ 2 phương pháp tính lãi: `FLAT` và `REDUCING_BALANCE`
- Tính lãi tự động hàng ngày qua Scheduler
- Xử lý thanh toán theo nguyên tắc interest-first allocation
- Phân tích 4 chỉ số: DTI, Interest Ratio, Overdue Ratio, Repayment Time
- Mô phỏng 2 chiến lược trả nợ: Minimize Interest (Avalanche) và Improve Cashflow
- Lưu kế hoạch trả nợ đã chọn kèm lịch trình chi tiết từng tháng
- Lưu trữ thông tin thu nhập/chi tiêu hàng tháng để phục vụ phân tích

## 2. Kiến trúc hệ thống

```
Client (React)
      ↓ HTTP/REST + JWT Bearer Token
Spring Security Filter Chain (JwtAuthenticationFilter)
      ↓
Controller Layer
      ↓
Service Layer
      ↓
Repository Layer (Spring Data JPA)
      ↓
PostgreSQL
```

Hệ thống sử dụng **Feature-Based Architecture** kết hợp **Layered Architecture** bên trong từng module. Mỗi feature được tổ chức độc lập, bao gồm đầy đủ Controller, Service, Repository, DTO và Mapper.

---

## 3. Kiến trúc module

| Module | Chức năng chính |
|--------|-----------------|
| **auth** | Đăng ký, đăng nhập, refresh JWT, token rotation |
| **user** | Quản lý hồ sơ người dùng, đổi mật khẩu |
| **debt** | CRUD khoản nợ, tính lãi, quản lý trạng thái, soft-delete |
| **payment** | Xử lý thanh toán, soft-delete, soft-update |
| **planning** | So sánh 2 kế hoạch (stateless), lưu/xem/xóa kế hoạch đã chọn |
| **analysis** | Phân tích 4 chỉ số sức khỏe tài chính |
| **dashboard** | Tổng hợp thông tin tài chính tổng quan |
| **scheduler** | Tác vụ định kỳ hàng ngày: cộng lãi, refresh trạng thái |

---

## 4. Thiết kế tầng hệ thống

### 4.1 Controller Layer
- Nhận request HTTP, xác thực user hiện tại qua `@AuthenticationPrincipal`
- Validate dữ liệu đầu vào qua Bean Validation (`@Valid`)
- Gọi Service Layer và trả về `ApiResponse<T>`
- Tất cả endpoint (ngoại trừ `/api/auth/**`) yêu cầu Bearer Token qua `@SecurityRequirement`

### 4.2 Service Layer & Business Rules

**DebtService**
- Tạo khoản nợ, tính `expectedMonthlyPayment` theo đúng công thức (FLAT hoặc REDUCING_BALANCE) qua `InterestCalculationService`
- Soft-delete khoản nợ (`deleted = true`)
- Update chỉ cho phép sửa `lenderName` (các trường tài chính không thể thay đổi sau khi tạo)

**InterestCalculationService + Strategy Pattern**
- Interface `InterestCalculationStrategy` với 2 method:
  - `calculateInterest(debt, fromDate, toDate)` — dùng cho accrual hàng ngày
  - `calculateMonthlyPayment(principal, termMonths, annualRate)` — dùng khi tạo debt
- `FlatInterestCalculationStrategy`: tính lãi trên `totalPrincipal`
- `ReducingBalanceInterestCalculationStrategy`: tính lãi trên `remainingPrincipal`, công thức amortization chuẩn

**InterestAccrualService**
- Cộng dồn lãi từ `lastInterestAccruedDate` đến ngày chỉ định
- Cập nhật `accruedInterest` và `lastInterestAccruedDate` trên Debt

**DebtStateService**
- `refreshDebtStatus`: `PAID_OFF` nếu `totalOutstanding ≤ 0`; `OVERDUE` nếu quá `nextDueDate`; else `ACTIVE`
- `moveNextDueDate`: chuyển `nextDueDate` sang tháng tiếp theo nếu payment >= `expectedMonthlyPayment`
- `calculateFirstDueDate`: tính ngày đến hạn đầu tiên từ `startDate` và `dueDay`

**PaymentService**
- Validate: debt tồn tại, chưa `PAID_OFF`, ngày thanh toán hợp lệ
- Accrual lãi đến ngày thanh toán trước khi phân bổ
- Interest-first allocation: trừ `accruedInterest` trước, phần còn lại vào `remainingPrincipal`
- Soft-delete (`deleted = true`) và soft-update (chỉ `note` và `paymentDate`)

**PlanningService + SimulationEngine**
- `comparePlans`: verify ownership, convert `Debt → DebtSnapshot` (in-memory), chạy `SimulationEngine.simulate()` độc lập cho 2 strategy, trả về `CompareResponse`, không lưu DB
- `SimulationEngine.simulate()`: chạy vòng lặp tháng, build `PlanComparisonDto` trực tiếp.
- `savePlan`: re-simulate với strategy đã chọn, `SavedPlan` + `PlanMonthlySchedule` + `PlanDebtPayment` vào DB, replace plan cũ nếu có
- `getSavedPlan`: load từ DB, trả về full schedule
- `deleteSavedPlan`: xóa plan, xóa schedules và debt payments

**AnalysisService**
- DTI: `totalActiveExpectedMonthlyPayment / monthlyIncome × 100`
- Interest Ratio: `totalAccruedInterest / monthlyIncome × 100`
- Overdue Ratio: `overdueDebtCount / (activeDebtCount + overdueDebtCount) × 100`
- Repayment Time: `totalRemainingDebt / totalActiveExpectedMonthlyPayment` (tháng)

**DebtScheduler**
- Chạy daily tại `00:00` server time
- Xử lý batch 100 debts/page, chỉ lấy non-`PAID_OFF`
- Với mỗi khoản nợ: cộng lãi phát sinh đến ngày hôm nay → cập nhật trạng thái (ACTIVE / OVERDUE / PAID_OFF)

### 4.3 Repository Layer
- CRUD qua Spring Data JPA
- Custom queries: tổng hợp (`SUM`), đếm theo status, filter theo `userId + status + deleted`

---

## 5. Cơ sở dữ liệu

### Bảng chính

| Bảng | Mô tả |
|------|-------|
| `users` | Thông tin người dùng, `monthlyIncome`, `expense` |
| `debts` | Khoản nợ, embedded `InterestSettings`, soft-delete |
| `payments` | Lịch sử thanh toán thực tế, soft-delete |
| `refresh_token` | JWT refresh token rotation (1:1 với user) |
| `saved_plans` | Kế hoạch trả nợ user đã chọn sau khi compare (1:1 với user) |
| `plan_monthly_schedules` | Lịch thanh toán dự kiến theo từng tháng của kế hoạch |
| `plan_debt_payments` | Phân bổ thanh toán chi tiết cho từng khoản nợ trong mỗi tháng |

### Quan hệ
- `User` (1) → (N) `Debt`
- `Debt` (1) → (N) `Payment`
- `User` (1) → (1) `RefreshToken`
- `User` (1) → (1) `SavedPlan`
- `SavedPlan` (1) → (N) `PlanMonthlySchedule`
- `PlanMonthlySchedule` (1) → (N) `PlanDebtPayment`
- `Debt` (1) → (N) `PlanDebtPayment` *(tham chiếu khoản nợ trong kế hoạch)*

### InterestSettings (Embedded trong Debt)
- `interestCalculationMethod`: `FLAT` | `REDUCING_BALANCE`
- `interestFrequency`: `DAILY` | `MONTHLY` | `ANNUALLY`
- `interestRate`: `BigDecimal` (% năm, ví dụ `12.0` = 12%/năm)

Chi tiết schema các bảng, thuộc tính và ràng buộc được trình bày trong [DATABASE_DESIGN.md](DATABASE_DESIGN.md).

---

## 6. Bảo mật

- **JWT stateless authentication**: access token (15 phút) + refresh token (7 ngày)
- **Token rotation**: mỗi lần refresh, token cũ bị xóa, token mới được lưu DB
- **BCrypt** mã hóa mật khẩu
- **Bean Validation** validate toàn bộ request input
- **Ownership check** trong mọi service: verify `userId` trước khi xử lý
- Chưa triển khai RBAC — tất cả authenticated users có quyền ngang nhau trên data của chính mình
- CORS: chỉ cho phép `http://localhost:3000`

---

## 7. Data Flow (Luồng xử lý)

### 7.1 Tạo khoản nợ

```
User → POST /api/debts
  → DebtController → DebtService
  → Map DTO → Debt entity
  → Tính expectedMonthlyPayment qua InterestCalculationStrategy:
     - FLAT:              M = P/n + P × (rate/100/12)
     - REDUCING_BALANCE:  M = P × [r(1+r)^n] / [(1+r)^n - 1]
  → Tính firstDueDate từ startDate + dueDay
  → Save Debt → DebtResponseDTO
```

### 7.2 Tạo thanh toán

```
User → POST /api/payments
  → PaymentService.createPayment()
  → Validate: debt tồn tại, chưa PAID_OFF, ngày hợp lệ
  → InterestAccrualService: cộng lãi từ lastInterestAccruedDate → paymentDate
  → Interest-first allocation:
       interestPaid  = min(amount, accruedInterest)
       principalPaid = min(amount - interestPaid, remainingPrincipal)
  → Cập nhật debt: accruedInterest, remainingPrincipal, lastPaymentDate
  → DebtStateService.moveNextDueDate()
  → DebtStateService.refreshDebtStatus() → PAID_OFF / OVERDUE / ACTIVE
  → Save Debt + Save Payment → PaymentResponseDTO
```

### 7.3 Cập nhật tự động hàng ngày

```
00:00 Daily → DebtScheduler
  → findByDeletedFalseAndStatusNot(PAID_OFF) — batch 100
  → Per debt:
      InterestAccrualService.accrueInterest(debt, today)
      DebtStateService.refreshDebtStatus(debt)
  → Save batch
```

### 7.4 So sánh kế hoạch trả nợ

```
User → POST /api/planning/compare
  { debtIds, monthlyExtraPayment, firstStrategy, secondStrategy }
  → PlanningService.comparePlans()
  → Verify ownership từng debtId
  → SnapshotMapper: Debt → DebtSnapshot (in-memory)
       balance        = remainingPrincipal + accruedInterest
       interestRate   = interestSettings.interestRate
       minimumPayment = expectedMonthlyPayment
  → SimulationEngine.simulate() × 2 (copy snapshots, chạy độc lập)
      Mỗi tháng:
        applyMonthlyInterest:   balance × (rate/100/12)
        applyMinimumPayments:   trừ minimumPayment từng debt
        strategy.selectTarget(): chọn debt ưu tiên
        applyExtraPayment:      trừ extra vào target
        releaseCashflow:        debt paid off → minimumPayment vào extra
        build DebtPaymentDetailDto per debt → SimulationMonthDto
  → PlanComparisonDto trả trực tiếp 
  → CompareResponse { firstPlan, secondPlan }   — KHÔNG lưu DB
```

**Chiến lược chọn debt ưu tiên:**
- `MINIMIZE_INTEREST` (Avalanche): chọn debt có `interestRate` cao nhất
- `IMPROVE_CASHFLOW`: chọn debt có `score = minimumPayment / estimatedPayoffMonths` cao nhất

### 7.5 Lưu kế hoạch trả nợ

```
User → POST /api/planning/save
  { debtIds, monthlyExtraPayment, strategy }
  → PlanningService.savePlan()
  → Verify ownership, re-simulate với strategy đã chọn
  → Xóa SavedPlan cũ nếu có (cascade xóa schedules + debt_payments)
  → Persist SavedPlan → PlanMonthlySchedule[] → PlanDebtPayment[]
  → SavedPlanResponse (full schedule)

User → GET  /api/planning/saved    → load từ DB, trả về full schedule
User → DELETE /api/planning/saved  → xóa plan + cascade
```

### 7.6 Phân tích sức khỏe tài chính

```
User → GET /api/analysis/all
  → AnalysisService.calculateAllAnalysis()
  → 4 chỉ số:
      DTI             = totalActiveExpectedMonthlyPayment / monthlyIncome × 100
                        GOOD: <30%    WARNING: 30–50%    CRITICAL: >50%
      Interest Ratio  = totalAccruedInterest / monthlyIncome × 100
                        GOOD: <10%    WARNING: 10–20%    CRITICAL: >20%
      Overdue Ratio   = overdueCount / (activeCount + overdueCount) × 100
                        GOOD: <30%    WARNING: 30–50%    CRITICAL: >50%
      Repayment Time  = totalRemainingDebt / totalActiveExpectedMonthlyPayment (tháng)
  → AnalysisResponse { dti, interestRatio, overdue, repaymentTime, analysisDate }
```

---

## 8. API Endpoints

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/auth/register` | Đăng ký tài khoản |
| POST | `/api/auth/login` | Đăng nhập, nhận access + refresh token |
| POST | `/api/auth/refresh` | Refresh access token |
| GET | `/api/users/me` | Lấy profile người dùng |
| PUT | `/api/users/me` | Cập nhật profile (fullName, monthlyIncome, expense) |
| POST | `/api/users/change-password` | Đổi mật khẩu |
| GET | `/api/dashboard` | Dashboard tổng quan |
| POST | `/api/debts` | Tạo khoản nợ |
| GET | `/api/debts` | Danh sách khoản nợ (filter by `status`) |
| GET | `/api/debts/{id}` | Chi tiết khoản nợ |
| PUT | `/api/debts/{id}` | Cập nhật khoản nợ |
| DELETE | `/api/debts/{id}` | Xóa khoản nợ (soft-delete) |
| GET | `/api/debts/{id}/payments` | Lịch sử thanh toán theo khoản nợ |
| POST | `/api/payments` | Ghi nhận thanh toán |
| GET | `/api/payments` | Toàn bộ lịch sử thanh toán |
| GET | `/api/payments/{id}` | Chi tiết thanh toán |
| PUT | `/api/payments/{id}` | Cập nhật `note` / `paymentDate` (soft-update) |
| DELETE | `/api/payments/{id}` | Xóa thanh toán (soft-delete) |
| GET | `/api/analysis/all` | Phân tích 4 chỉ số sức khỏe tài chính |
| POST | `/api/planning/compare` | So sánh 2 kế hoạch trả nợ (stateless) |
| POST | `/api/planning/save` | Lưu kế hoạch đã chọn |
| GET | `/api/planning/saved` | Xem kế hoạch đã lưu |
| DELETE | `/api/planning/saved` | Xóa kế hoạch đã lưu |

---

## 9. Design Patterns sử dụng

| Pattern | Áp dụng tại |
|---------|-------------|
| Feature-Based + Layered Architecture | Toàn bộ project |
| Strategy Pattern | `InterestCalculationStrategy` (FLAT/REDUCING_BALANCE), `DebtSelectionStrategy` (Avalanche/Cashflow) |
| Factory Pattern | `InterestCalculationStrategyFactory` |
| DTO Pattern | Tất cả Controller ↔ Service boundary |
| Mapper Pattern | `DebtMapper`, `PaymentMapper`, `SnapshotMapper`, `SavedPlanMapper` |
| Scheduler Pattern | `DebtScheduler` — daily cron job |
| Simulation Engine Pattern | `SimulationEngine` — stateless monthly simulation loop, trả thẳng DTO |
| Soft Delete Pattern | `Debt.deleted`, `Payment.deleted` |
