# **Software Architecture Document (SAD) - DebtWizard**

## 1. Giới thiệu

### 1.1 Mục tiêu hệ thống

**Problem**
Nhiều người đang mắc nhiều khoản nợ cùng lúc nhưng không biết:
  + Nên ưu tiên trả khoản nào trước
  + Nếu có thêm tiền dư thì nên phân bổ như thế nào
  + Kế hoạch nào giúp tiết kiệm lãi nhiều nhất
  + Kế hoạch nào giúp giảm áp lực thanh toán hàng tháng
  + Khi nào có thể thoát nợ

**System Goal**
- Quản lý các khoản nợ
- Phân tích dòng tiền khả dụng
- Sinh và mô phỏng các kế hoạch trả nợ theo nhiều mục tiêu tài chính
- So sánh kết quả giữa các kế hoạch trả nợ
- Phát hiện các cơ hội tất toán sớm (Quick Win Opportunities)
- Hỗ trợ người dùng đưa ra quyết định trả nợ phù hợp với mục tiêu tài chính

### 1.2 Phạm vi hệ thống
- Quản lý các khoản nợ cá nhân và lịch sử thanh toán
- Tính lãi tự động và theo dõi dư nợ còn lại
- Lưu trữ thông tin thu nhập và chi phí hàng tháng của người dùng để phục vụ phân tích khả năng trả nợ.
- Sinh và mô phỏng nhiều kế hoạch trả nợ theo các mục tiêu tài chính khác nhau
- Tối ưu phân bổ khoản tiền dư (Extra Payment) giữa các khoản nợ
- So sánh các kế hoạch dựa trên tổng lãi, thời gian tất toán và áp lực dòng tiền
- Hỗ trợ ra quyết định thông qua so sánh kế hoạch trả nợ và phát hiện các cơ hội tất toán sớm
---

<img src="docs/images/Usecase.drawio.png" style="width:30%; height:auto;">

### 1.3 Quy trình tổng quát
<img src="docs/images/OverallUserWorkflow.drawio.png" style="width:30%; height:auto;">

## 2. Kiến trúc hệ thống
Client (React)
      ↓
Controller Layer
      ↓
Service Layer
      ↓
Repository Layer
      ↓
PostgreSQL

Hệ thống sử dụng kiến trúc **Feature-Based Architecture kết hợp Layered Architecture bên trong từng module**.
Mỗi feature được tổ chức độc lập, bao gồm đầy đủ các tầng Controller, Service và Repository.
  - Controller: Xử lý request
  - Service: Xử lý logic nghiệp vụ
  - Repository: Truy cập dữ liệu

<img src="docs/images/SystemArchitectureDiagram.drawio.png" style="width:100%; height:auto;">
---

## 3. Kiến trúc module
| Module               | Chức năng chính                                                |
|----------------------|----------------------------------------------------------------|
| **Auth&UserProfile** | Authentication & User Profile Management                                   |
| **Debt**             | Quản lý khoản nợ, tính lãi, quản lý trạng thái nợ                                    |
| **Payment**          | Xử lý thanh toán                             | 
| **Planning**         | Sinh, mô phỏng và so sánh kế hoạch trả nợ                      |
| **Analysis**         | Phân tích sức khỏe tài chính |
| **Scheduler**        | Tác vụ định kỳ (cộng lãi, refresh trạng thái)                  |
---

## 4. Thiết kế tầng hệ thống

### 4.1 Controller Layer

  * Nhận request từ client, xác thực User hiện tại.
  * Validate dữ liệu đầu vào.
  * Gọi Service Layer.
  * Trả DTO Response

### 4.2 Service Layer & Business Rules

Chứa toàn bộ business logic:

  - Quản lý khoản nợ
  - Tính lãi phát sinh
  - Xử lý thanh toán
  - Phân tích dòng tiền
  - Sinh kế hoạch trả nợ
  - Mô phỏng trả nợ
  - So sánh các kế hoạch
  - Tính toán chỉ số tài chính

### 4.3 Repository Layer

  - CRUD dữ liệu qua JPA/Hibernate
  - Truy vấn tổng hợp (aggregate queries)
  - Truy vấn thống kê

---

## 5. Cơ sở dữ liệu

Hệ thống gồm các bảng chính:

  * users
  * debts
  * payments
  * repayment_plans

### Quan hệ:
  * User (1) → (N) Debt
  * Debt (1) → (N) Payment
  * User (1) → (N) RepaymentPlan
    ![ERD](docs/images/Erd.png)
---

## 6. Bảo mật hệ thống
- Hệ thống sử dụng JWT theo mô hình stateless authentication.
- Mật khẩu người dùng được mã hóa bằng BCrypt trước khi lưu vào cơ sở dữ liệu.
- Toàn bộ request được validate bằng Bean Validation nhằm đảm bảo tính hợp lệ của dữ liệu đầu vào.
- Hệ thống hiện chỉ hỗ trợ một loại người dùng (User), chưa triển khai Role-Based Access Control (RBAC).

## 7. Data Flow (Luồng xử lý)

### 7.1 Tạo khoản nợ
```text id ="flow1"
User → DebtController → DebtService
→ Validate Debt Information + map DTO
→ Khởi tạo debt
→ save Debt
→ trả DebtResponseDTO
```
### 7.2 Tạo thanh toán

```text id="flow2"
User → PaymentController → PaymentService (createPayment)
  ├─ Load Debt & Validate Business Rules
      ├─ Debt tồn tại
      ├─ Debt chưa PAID_OFF
      ├─ Payment Date hợp lệ
      └─ Nếu FAIL → ErrorResponseDTO
  DebtInterestService
      └─ Tính lãi phát sinh từ LastPaymentDate đến PaymentDate
  ├─> PaymentAllocationService:
  │    └─> Thanh toán lãi phát sinh trước, phần còn lại vào gốc.
  ├─> Cập nhật thông tin Debt:
  │    ├─> Cập nhật LastPaymentDate
       ├─> Cập nhật RemainingBalance
       ├─> DebtStateService: Dịch chuyển hạn trả nợ tiếp theo (moveNextDueDate)
  │    └─> DebtStateService: Cập nhật trạng thái (refreshDebtStatus) -> PAID_OFF nếu nợ = 0
  └─> Save Debt & Save Payment → Trả về PaymentResponseDTO.
---
```
### 7.3 Dashboard tổng quan tài chính

```text id="flow3"
Request → DashboardController → DashboardService
├─> Aggregate Financial Metrics
│     ├─ Total Debt
│     ├─ Remaining Debt
│     ├─ Accrued Interest
│     ├─ Overdue Debt
│     └─ Available Extra Payment
├─> Due Date Analysis
│     └─ Next Due Debt Information
├─> Opportunity Analysis
│     └─ Quick Win Opportunities
└─> DashboardResponseDTO
```
### 7.4 Luồng cập nhật tự động hàng ngày
```text id ="flow4"
System Trigger (00:00 Daily) → DebtScheduler
  ├─> Lấy danh sách toàn bộ khoản nợ đang hoạt động (Status != PAID_OFF)
  ├─> DebtInterestService: Tính toán và cộng dồn lãi suất tự động
  └─> DebtStateService: Kiểm tra và chuyển trạng thái (VD: từ ACTIVE sang OVERDUE)
```

### 7.5 Sinh kế hoạch trả nợ
```text id ="flow5"
User → RepaymentPlanController → RepaymentPlanService
├─> Lấy danh sách Active Debts
├─> Load User Financial Profile
│     ├─ Monthly Income
│     ├─ Monthly Expense
│     └─ Available Extra Payment Budget = Monthly Income - Monthly Expense - Total Minimum Payments
├─> Generate Repayment Strategies
│     Save Money Plan
│     ├─ Interest Priority Score = Annual Percentage Rate
│     └─ Ưu tiên các khoản nợ có Interest Priority Score cao hơn
│        nhằm giảm tổng chi phí lãi vay.
│    Improve Cash Flow Plan
        Estimated Payoff Months = Remaining Balance/(Minimum Payment + Extra Payment Allocation)
        ├─ Priority Score
│           = Monthly Payment/ Estimated Payoff Months
        └─ Ưu tiên các khoản nợ có:
            - Khoản thanh toán hàng tháng lớn
            - Có thể tất toán trong thời gian ngắn
├─> Repayment Simulator
├─> Plan Comparison
├─> Debt Payoff Opportunity Analysis
│     ├─ Quick Win Score
│     │      = Monthly Payment Released × Expected Duration Reduction / Additional Payment Required
│     │      (đo lợi ích dòng tiền trên mỗi đồng chi thêm — lợi ích / chi phí)
│     │
│     └─ Phát hiện các khoản nợ có thể tất toán sớm
│        với chi phí thấp nhưng mang lại lợi ích dòng tiền.
└─> RepaymentPlanResponseDTO
```

### 7.6 Mô phỏng trả nợ
Đối với mỗi kế hoạch:
Trong khi vẫn còn khoản nợ chưa tất toán:
- Tính lãi phát sinh cho từng khoản nợ
- Áp dụng Minimum Payment cho tất cả khoản nợ đang hoạt động
- Xác định khoản nợ ưu tiên theo chiến lược của kế hoạch
- Phân bổ Extra Payment vào khoản nợ ưu tiên
- Cập nhật Remaining Balance
- Kiểm tra các khoản nợ đã tất toán
- Giải phóng Monthly Payment của các khoản nợ đã tất toán
- Tự động cộng phần Monthly Payment được giải phóng vào Extra Payment Budget
- Chuyển sang tháng tiếp theo

Kết thúc khi:
Tất cả khoản nợ đều ở trạng thái PAID_OFF.


### 7.7 So sánh kế hoạch
Compare:
- Total Interest
- Debt-Free Date
- Total Payment
- Monthly Cash Flow Released
- Months Saved
↓
PlanComparisonResponseDTO
### 7.8 Phân tích sức khỏe tài chính
```text id ="flow6"
User → Controller → AnalysisService (calculateAllAnalysis)
    ├── Financial Analysis
    │      ├── DtiResponse: Tỷ lệ nợ trên thu nhập (Debt-to-Income Ratio)
    │      ├── InterestRatioResponse: Đánh giá khả năng chi trả lãi từ thu nhập hiện tại
    │      ├── OverdueRatioResponse: Tỷ lệ khoản nợ quá hạn trên tổng số khoản nợ đang hoạt động
    │      └── RepaymentTimeResponse: Ước lượng thời gian tất toán toàn bộ khoản nợ
    ↓
    Financial Health Classification
    - GOOD
    - WARNING
    - CRITICAL
    ↓
AnalysisResponseDTO
```

---


## 8. Design Patterns sử dụng
  * Feature-Based Architecture
  * Layered Architecture
  * Controller-Service-Repository Pattern
  * Service Layer Pattern
  * Strategy Pattern
    - SaveMoneyStrategy
    - CashflowStrategy
  * DTO Pattern
  * Scheduler Pattern
  * Simulation Engine Pattern.

---
