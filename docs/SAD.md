# **Software Architecture Document (SAD) - DebtWizard**

## 1. Giới thiệu

### 1.1 Mục tiêu hệ thống

DebtWizard là hệ thống web quản lý nợ cá nhân, giúp người dùng theo dõi khoản nợ, thanh toán, tính lãi và tối ưu chiến lược trả nợ dựa trên dữ liệu và phân tích thống kê.

### 1.2 Phạm vi hệ thống

Hệ thống hỗ trợ:

  * Quản lý khoản nợ.
  * Quản lý thanh toán.
  * Tính lãi tự động và cộng dồn theo thời gian thực.
  * Thống kê tình hình tài chính và sức khỏe tài chính (DTI).
  * Gợi ý chiến lược trả nợ (Snowball / Avalanche).
  * Phân tích dữ liệu bằng R theo hai hướng:
      + Ước lượng thời gian tất toán nợ, theo dõi tiến độ trả nợ.
      + Thực nghiệm thống kê đa biến (PCA, Factor Analysis, MANOVA, Clustering).

### 1.3 Hướng phát triển (Planned Features)

  - Mô phỏng tài chính (Simulation) giúp người dùng thử nghiệm các kịch bản trả nợ khác nhau.
  - Tích hợp hệ thống AI Recommendation để đề xuất chiến lược trả nợ tối ưu (Snowball, Avalanche hoặc hybrid strategy).
  - Hệ thống thông báo (Notification System) nhắc lịch thanh toán.
  - Cơ chế Gamification nhằm tăng động lực trả nợ (streak, achievement, progress reward)

---

## 2. Kiến trúc hệ thống

Hệ thống sử dụng kiến trúc **Feature-Based Architecture kết hợp Layered Architecture bên trong từng module**.
Mỗi feature được tổ chức độc lập, bao gồm đầy đủ các tầng Controller, Service và Repository.
  - Controller: Xử lý request
  - Service: Xử lý logic nghiệp vụ
  - Repository: Truy cập dữ liệu

```text id="arch1"
Backend (Spring Boot - Feature-Based Architecture)

    ├── Auth Feature
    ├── Debt Feature
    ├── Payment Feature
    ├── Interest Feature
    ├── Analysis Feature
    └── Recommendation Feature

            ↓

   (Controller → Service → Repository)

            ↓

        PostgreSQL Database
```

---

## 3. Kiến trúc module

Hệ thống backend được chia theo Feature-Based Architecture, trong đó mỗi module đại diện cho một domain nghiệp vụ độc lập:

  - **Auth Module**: Xử lý đăng ký, đăng nhập và xác thực phiên làm việc (session authentication).
  - **Debt Module**: Quản lý thông tin, vòng đời và trạng thái các khoản nợ của người dùng.
  - **Payment Module**: Phân bổ dòng tiền thanh toán vào gốc/lãi và ghi nhận giao dịch.
  - **Interest Module**: Chịu trách nhiệm tính toán lãi suất (Calculation) và cộng dồn lãi định kỳ (Accrual).
  - **Summary Module**: Tổng hợp dữ liệu tài chính và trích xuất khoản nợ sắp đến hạn.
  - **Analysis Module**: Đánh giá sức khỏe tài chính (DTI) và phân tích thống kê qua R.
  - **Recommendation Module**: Đề xuất ưu tiên trả nợ dựa trên chiến lược (Snowball, Avalanche).

---

## 4. Thiết kế tầng hệ thống

### 4.1 Controller Layer

  * Nhận request từ client, xác thực User hiện tại
  * Validate dữ liệu đầu vào
  * Trả về response qua DTO.

### 4.2 Service Layer & Business Rules

  * Chứa toàn bộ logic nghiệp vụ cốt lõi. Một số quy tắc nghiệp vụ (Business Rules) quan trọng được implement tại đây:
    - Quy tắc phân bổ thanh toán (Payment Application Rule):
      + Interest First: Tiền thanh toán sẽ trừ vào phần Lãi (Accrued Interest) trước, dư mới trừ vào Gốc (Remaining Principal).
      + Principal First: Ưu tiên trừ vào Gốc trước, dư mới trừ vào Lãi.
    - Ràng buộc thời gian thanh toán: Hệ thống nghiêm cấm thanh toán ảo (ngày ở tương lai), cấm thanh toán trước khi khoản nợ bắt đầu, và cấm thanh toán lùi thời gian so với lần trả gần nhất.
    - Đánh giá sức khỏe tài chính (DTI): Tỷ lệ nợ trên thu nhập được phân loại thành 3 mức: GOOD ($\le 30\%$), WARNING ($\le 50\%$), và CRITICAL ($> 50\%$).

### 4.3 Repository Layer

  * Giao tiếp với database
  * CRUD thông qua JPA/Hibernate

---

## 5. Cơ sở dữ liệu

Hệ thống gồm các bảng chính:

  * users
  * debts
  * payments
  * interest_configs

### Quan hệ:

  * User (1) → (N) Debt
  * Debt (1) → (N) Payment
  * Debt (1) → (1) InterestConfig

📌 Xem chi tiết ERD trong file: `ERD.md`

---

## 6. Bảo mật hệ thống

* Xác thực bằng Cookie-based Session.
* Mã hóa mật khẩu bằng BCrypt trước khi lưu vào cơ sở dữ liệu.
* Mọi truy vấn chi tiết (Debt, Payment) đều phải xác thực khớp với userId của User đang đăng nhập.
* Hệ thống hiện tại hỗ trợ một loại người dùng (User), không triển khai phân quyền nhiều vai trò.
* Hỗ trợ xác thực qua Google OAuth2.

---

## 7. Data Flow (Luồng xử lý)

### 7.1 Tạo khoản nợ
```text id ="flow1"
User → Controller → DebtService 
  ├─> Map DTO to Entity (DebtMapper)
  ├─> DebtStateService: Tính toán ngày hạn trả đầu tiên (calculateFirstDueDate)
  ├─> DebtService: Tính toán số tiền trả dự kiến hàng tháng (Expected Monthly Payment)
  ├─> Khởi tạo InterestConfig (InterestConfigMapper)
  └─> Lữu dữ liệu (DebtRepository) → Trả về DebtResponse
```
### 7.2 Tạo thanh toán

```text id="flow2"
User → Controller → PaymentService (createPayment)
  ├─> Lấy dữ liệu Debt & Validate nghiệp vụ (Ngày thanh toán hợp lệ, chưa PAID_OFF)
  ├─> InterestAccrualService: Tính toán và cộng dồn lãi suất đến ngày thanh toán
  ├─> PaymentAllocationService (Phân bổ dòng tiền):
  │    └─> Dựa vào Rule (INTEREST_FIRST/PRINCIPAL_FIRST) để trừ tiền Lãi và Gốc
  ├─> Cập nhật thông tin Debt:
  │    ├─> Cập nhật LastPaymentDate
  │    ├─> DebtStateService: Dịch chuyển hạn trả nợ tiếp theo (moveNextDueDate)
  │    └─> DebtStateService: Cập nhật trạng thái (refreshDebtStatus) -> PAID_OFF nếu nợ = 0
  └─> Save Debt & Save Payment → Trả về PaymentResponse (PaymentMapper)
```

---

### 7.3 Tính tổng nợ (Summary)

```text id="flow3"
Request → Controller → SummaryService
  ├─> Truy xuất Aggregate Queries từ Repository (Total Debt, Remaining, Overdue, Accrued Interest...)
  ├─> Phân tích Logic (Next Due Debt Info):
  │    └─> Duyệt các khoản nợ ACTIVE, dùng ChronoUnit tính khoảng cách ngày đến hạn gần nhất
  └─> Trả về SummaryResponse
```
### 7.4 Luồng cập nhật tự động hàng ngày
```text id ="flow4"
System Trigger (00:00 Daily) → DebtScheduler
  ├─> Lấy danh sách toàn bộ khoản nợ đang hoạt động (Status != PAID_OFF)
  ├─> InterestAccrualService: Tính toán và cộng dồn lãi suất tự động
  └─> DebtStateService: Kiểm tra và chuyển trạng thái (VD: từ ACTIVE sang OVERDUE)
```

### 7.5 Đề xuất chiến lược trả nợ (Debt Recommendation)
```text id ="flow5"
User → Controller → DebtRecommendationService 
  ├─> Lấy danh sách Active Debts 
  ├─> Apply Strategy Pattern:
  │    ├─> Snowball: Sắp xếp ưu tiên theo Dư nợ gốc tăng dần.
  │    └─> Avalanche: Sắp xếp ưu tiên theo Lãi suất giảm dần.
  └─> Map to DTO (DebtRecommendationMapper) → Return DebtRecommendationResponse
```
### 7.6 Đánh giá sức khỏe tài chính (DTI Analysis)
* Công thức áp dụng:
  - DTI = (Total Active Expected Monthly Payment / Monthly Income) × 100
```text id ="flow6"
User → Controller → AnalysisService (calculateCurrentDti)
  ├─> Lấy Tổng thu nhập (Monthly Income) & Tổng chi trả dự kiến (Expected Monthly Payment)
  ├─> Tính toán DTI Ratio
  └─> Đánh giá trạng thái (GOOD / WARNING / CRITICAL) → Return DtiResponse
```
---

## 8. Design Patterns sử dụng

  * **Layered Architecture**(Controller – Service – Repository trong từng feature)
  * **Service Layer Pattern** (tách business logic khỏi controller)
  * **ORM Pattern** (JPA/Hibernate Entity Mapping)
  * **Strategy Pattern** Áp dụng trong tính lãi (InterestCalculationMethod) và phân bổ trả nợ (Snowball / Avalanche).
  * **Mapper / DTO Pattern**: Sử dụng các class Mapper để chuyển đổi dữ liệu an toàn giữa Client và Database.
  * **Scheduled Task / Background Worker Pattern**: Ứng dụng trong DebtScheduler để xử lý tác vụ ngầm định kỳ.


---

## 9. Hệ thống phân tích (R Integration)

Hệ thống tích hợp R để thực hiện các phân tích thống kê đa biến (Multivariate Statistical Analysis) nhằm khai thác dữ liệu tài chính và hỗ trợ đánh giá mô hình trả nợ.

### Quy trình xử lý:
  - Xuất dữ liệu từ cơ sở dữ liệu (PostgreSQL)
  - Tiền xử lý và chuẩn hóa dữ liệu
  - Thực hiện phân tích bằng R

### Các phương pháp sử dụng:
  - PCA (Principal Component Analysis)
  - Factor Analysis
  - MANOVA (Multivariate Analysis of Variance)
  - Clustering Analysis

---
## 10. Khả năng mở rộng

Hệ thống được thiết kế theo hướng dễ dàng mở rộng các module sau trong tương lai:

  * AI Recommendation Engine
  * Gamification System
  * Notification System
  * Financial Education Module
  * Analytics Dashboard

---
