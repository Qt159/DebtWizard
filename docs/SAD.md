# Software Architecture Document (SAD) — DebtWizard

---

## 1. Giới thiệu & Mục tiêu kiến trúc

Tài liệu Kiến trúc Phần mềm (SAD) mô tả toàn diện cấu trúc kỹ thuật, nguyên lý thiết kế, các tầng xử lý, mô hình dữ liệu và cơ chế hướng sự kiện của hệ thống **DebtWizard**.

### Mục tiêu thiết kế kiến trúc:
- **Độc lập tính năng (Feature-based Modular Design):** Mỗi nghiệp vụ được đóng gói độc lập, hạn chế tối đa phụ thuộc chéo (low coupling), giúp dễ mở rộng và bảo trì.
- **Tính toán tài chính chính xác (Financial Accuracy):** Sử dụng `BigDecimal` với quy tắc làm tròn chuẩn xác cho mọi phép toán lãi suất và phân bổ nợ.
- **Hiệu năng mô phỏng cao (High Performance Simulation):** Quá trình mô phỏng 600 tháng được tối ưu hóa in-memory trên `DebtSnapshot` để đạt độ trễ phản hồi thấp (<200ms).
- **Bảo mật và cô lập dữ liệu (Security & Isolation):** Xác thực phi trạng thái bằng JWT token rotation, kiểm tra quyền sở hữu đối tượng nghiêm ngặt trên từng request.
- **Kiến trúc hướng sự kiện linh hoạt (Event-Driven Extensibility):** Tách biệt nghiệp vụ thanh toán/nhắc nợ với cơ chế gửi thông báo bằng Event Publisher, sẵn sàng nâng cấp lên Amazon SQS và AWS Lambda.

---

## 2. Kiến trúc Tổng thể Hệ thống (High-Level Architecture)

```text
       ┌────────────────────────────────────────────────────────┐
       │                 Client (Web / Mobile App)              │
       └───────────────────────────┬────────────────────────────┘
                                   │ HTTP / HTTPS (REST API)
                                   │ Bearer JWT Token
                                   ▼
       ┌────────────────────────────────────────────────────────┐
       │               Spring Security Filter Chain             │
       │              (JwtAuthenticationFilter)                 │
       └───────────────────────────┬────────────────────────────┘
                                   │
                                   ▼
 ┌─────────────────────────────────────────────────────────────────────┐
 │                         SPRING BOOT BACKEND                         │
 │                                                                     │
 │  ┌───────────────────────────────────────────────────────────────┐  │
 │  │                       CONTROLLER LAYER                        │  │
 │  │ Auth | User | FinanceProfile | Debt | Payment | Plan | etc.   │  │
 │  └──────────────────────────────┬────────────────────────────────┘  │
 │                                 │ (DTO / Validated Input)           │
 │                                 ▼                                   │
 │  ┌───────────────────────────────────────────────────────────────┐  │
 │  │                        SERVICE LAYER                          │  │
 │  │  Business Logic • Transaction Management • Strategy Engine   │  │
 │  │  SimulationEngine • DebtScheduler • InterestCalculators       │  │
 │  └──────────────┬───────────────────────────────┬────────────────┘  │
 │                 │ (Spring Events)               │ (Domain Entities) │
 │                 ▼                               ▼                   │
 │  ┌───────────────────────────────┐ ┌─────────────────────────────┐  │
 │  │       EVENT & LISTENERS       │ │      REPOSITORY LAYER       │  │
 │  │  PaymentCompletedEvent        │ │    Spring Data JPA /        │  │
 │  │  PaymentReminderEvent         │ │    Hibernate ORM            │  │
 │  └───────────────────────────────┘ └────────────┬────────────────┘  │
 └─────────────────────────────────────────────────┼───────────────────┘
                                                   │ JDBC Connection
                                                   ▼
       ┌────────────────────────────────────────────────────────┐
       │             PostgreSQL Database (Amazon RDS)           │
       └────────────────────────────────────────────────────────┘
```

---

## 3. Kiến trúc Module (Feature-Based Architecture)

DebtWizard tổ chức mã nguồn theo **Feature-Based Architecture**. Mỗi module chức năng trong `com.tuan.debtwizard.features` tự chứa đầy đủ các tầng Controller, Service, Repository, Model, DTO và Mapper tương ứng.

```text
com.tuan.debtwizard
├── config/              # Security, OpenAPI Swagger, Web config
├── dto/                 # Generic ApiResponse<T>, Global shared DTOs
├── exception/           # GlobalExceptionHandler, AppException, ErrorCode
└── features/
    ├── auth/            # Xác thực, JWT Generation, Refresh Token Rotation
    ├── user/            # Quản lý tài khoản, UserProfile, Đổi mật khẩu
    ├── financeprofile/  # Hồ sơ thu nhập & chi phí thiết yếu (FinanceProfile)
    ├── debt/            # Quản lý nợ, Interest Engine, Debt Scheduler
    ├── payment/         # Xử lý thanh toán, Interest-First Allocation
    ├── planning/        # SimulationEngine, Strategies, SavedPlan Management
    ├── analysis/        # Phân tích 4 chỉ số tài chính (DTI, Interest, Overdue, Time)
    ├── dashboard/       # Thống kê tổng hợp số dư, lãi, nợ sắp đến hạn
    ├── notification/    # Xử lý thông báo in-app, Reminder Scheduler, Event Handlers
    └── event/           # Event Publisher interface & Event DTOs
```

| Module | Trách nhiệm chính |
|---|---|
| **auth** | Đăng ký tài khoản, Đăng nhập, Refresh Token, Đăng xuất, Sinh & kiểm tra JWT token. |
| **user** | Lấy thông tin người dùng (`/api/users/me`), đổi mật khẩu. |
| **financeprofile** | Quản lý thu nhập cố định và chi tiêu thiết yếu hàng tháng. |
| **debt** | CRUD khoản nợ, tính `expectedMonthlyPayment`, cộng lãi accrual hàng ngày (`DebtScheduler`), tính toán lãi suất (Flat vs Reducing Balance). |
| **payment** | Ghi nhận thanh toán, phân bổ Interest-First, cập nhật `nextDueDate` và trạng thái `PAID_OFF`, phát `PaymentCompletedEvent`. |
| **planning** | Mô phỏng và so sánh chiến lược trả nợ (Avalanche, Improve Cashflow) qua `SimulationEngine`, lưu/xem/xóa kế hoạch trả nợ. |
| **analysis** | Tính toán 4 chỉ số sức khỏe tài chính và phân loại (`GOOD`, `WARNING`, `CRITICAL`). |
| **dashboard** | Tổng hợp số liệu thống kê thời gian thực cho trang chủ. |
| **notification** | Lắng nghe event tạo thông báo thanh toán, chạy scheduler quét nợ sắp đến hạn (trước 3 ngày) tạo thông báo nhắc nợ, quản lý trạng thái đọc. |
| **event** | Cung cấp interface `EventPublisher` và các Record Event (`PaymentCompletedEvent`, `PaymentReminderEvent`). |

---

## 4. Thiết kế Chi tiết các Tầng (Layered Design)

### 4.1 Controller Layer
- Nhận request HTTP, trích xuất thông tin người dùng đã xác thực thông qua `@AuthenticationPrincipal UserDetails`.
- Thực hiện xác thực dữ liệu đầu vào (Bean Validation: `@Valid`, `@NotNull`, `@Positive`, v.v.).
- Ủy quyền xử lý cho Service Layer tương ứng và chuẩn hóa dữ liệu trả về qua `ApiResponse<T>`.
- Khai báo OpenAPI Swagger annotations (`@Operation`, `@SecurityRequirement(name = "Bearer Authentication")`).

### 4.2 Service Layer & Core Business Logic
- **Transaction Management:** Sử dụng `@Transactional` trên các thao tác ghi và `@Transactional(readOnly = true)` trên các tác vụ truy vấn để tối ưu hiệu năng và đảm bảo tính toàn vẹn ACID.
- **Interest Calculation Engine (Strategy Pattern):**
  - Giao diện `InterestCalculationStrategy` định nghĩa 2 phương thức:
    1. `calculateMonthlyPayment(principal, termMonths, annualRate)`: Tính số tiền trả cố định hàng tháng khi tạo khoản nợ.
    2. `calculateInterest(debt, fromDate, toDate)`: Tính lãi phát sinh hàng ngày trong khoảng thời gian xác định.
  - `FlatInterestCalculationStrategy`: Tính lãi đều trên dư nợ gốc ban đầu (`totalPrincipal`).
  - `ReducingBalanceInterestCalculationStrategy`: Tính lãi trên dư nợ gốc còn lại (`remainingPrincipal`) theo công thức Amortization chuẩn.
  - `InterestCalculationStrategyFactory`: Khởi tạo và cung cấp chiến lược tương ứng theo cấu hình `InterestCalculationMethod`.
- **Planning & Simulation Engine:**
  - `PlanningService`: Xác thực quyền sở hữu, tính `maxAllowedExtraPayment`, sao chép snapshot và điều phối mô phỏng.
  - `SimulationEngine`: Thực thi vòng lặp mô phỏng hàng tháng in-memory, áp dụng chiến lược lựa chọn nợ ưu tiên (`MinimizeInterestStrategy` hoặc `ImproveCashflowStrategy`), tính toán cashflow giải phóng và `snowballBonus`.
- **Debt State & Scheduler:**
  - `DebtScheduler`: Chạy vào `00:00` hàng ngày, lấy các khoản nợ chưa tất toán theo lô (Batch 100) để cộng dồn lãi phát sinh và làm mới trạng thái (`ACTIVE`, `OVERDUE`, `PAID_OFF`).
  - `PaymentReminderScheduler`: Quét các khoản nợ có ngày đến hạn sau 3 ngày và phát `PaymentReminderEvent`.

### 4.3 Repository Layer (Spring Data JPA)
- Cung cấp các thao tác CRUD tiêu chuẩn.
- Tối ưu hóa truy vấn bằng custom JPQL:
  - Sử dụng `JOIN FETCH` (ví dụ `findAllByIdWithUser`) để loại trừ hiện tượng N+1 Query.
  - Thực hiện các truy vấn tổng hợp (`SUM(expectedMonthlyPayment)`, `SUM(remainingPrincipal)`, `SUM(accruedInterest)`) trực tiếp tại database engine.
  - Hỗ trợ phân trang và sắp xếp thông qua `Pageable`, `Page<T>`.

---

## 5. Kiến trúc Hướng Sự kiện (Event-Driven Notification Architecture)

### 5.1 Hiện trạng: Spring In-App Event Architecture
DebtWizard triển khai kiến trúc Loose Coupling giữa module thanh toán / nhắc nợ và module thông báo:

```text
[PaymentService] ─────────► publish(PaymentCompletedEvent) ──┐
                                                             │
                                                             ▼
                                                [EventPublisher (Spring)]
                                                             │
[PaymentReminderScheduler] ─► publish(PaymentReminderEvent) ──┤
                                                             │
                                                             ▼
                                              [EventHandler / Listeners]
                                                             │
                                                             ▼
                                                   [NotificationService]
                                                             │
                                                             ▼
                                                   [PostgreSQL Database]
```

- `PaymentService` không gọi trực tiếp `NotificationService`. Sau khi giao dịch thanh toán hoàn tất, service chỉ phát sinh `PaymentCompletedEvent`.
- `PaymentCompletedEventHandler` lắng nghe sự kiện và yêu cầu `NotificationService` tạo bản ghi thông báo trong DB.
- `PaymentReminderScheduler` quét các khoản nợ đến hạn sau 3 ngày và phát `PaymentReminderEvent`.
- **Cơ chế chống trùng lặp (Idempotency):** Cột `referenceKey` trên bảng `notifications` được gắn ràng buộc `UNIQUE` ở cấp độ Database, ngăn chặn việc tạo trùng thông báo khi sự kiện được gửi lại.

### 5.2 Định hướng Mở rộng: Serverless Cloud Event Architecture (AWS SQS + Lambda + SES)
Khi mở rộng hệ thống lên nhiều phiên bản (Multi-instance EC2), kiến trúc thông báo sẽ được chuyển dịch sang mô hình Serverless phân tán:

```text
  [Spring Boot App (EC2)]
             │
             │ Publish Event JSON
             ▼
      [Amazon SQS Queue] ────── (Dead-Letter Queue - DLQ)
             │
             │ Event Trigger
             ▼
     [AWS Lambda Worker]
      ├── 1. Lưu In-App Notification (PostgreSQL / Amazon RDS)
      └── 2. Gửi Email thông báo (Amazon SES)
```

- **Lợi ích:**
  - Hoàn toàn phi đồng bộ, không làm tăng thời gian phản hồi của API thanh toán.
  - Tự động thử lại (Retry) và đưa vào Dead-Letter Queue (DLQ) nếu dịch vụ gửi email hoặc database tạm thời gián đoạn.
  - Chuyển `PaymentReminderScheduler` sang **Amazon EventBridge Scheduler** để tránh hiện tượng chạy lặp trên nhiều EC2 instance.

---

## 6. Thiết kế Bảo mật (Security Architecture)

```text
[Client Request] 
      │
      ▼
[JwtAuthenticationFilter]
      ├── Trích xuất Bearer Token từ Header Authorization
      ├── Validate Token (Signature & Expiration qua JwtService)
      ├── Load UserDetails từ Database
      └── Nạp Authentication vào SecurityContextHolder
            │
            ▼
[Controller / Service Layer]
      └── Kiểm tra Data Ownership: request.userId == securityContext.user.id
```

- **Stateless Authentication:** Sử dụng JSON Web Token (JJWT 0.12.5), không lưu session trên server backend.
- **Token Rotation:**
  - `accessToken`: Hiệu lực 15 phút (`900000ms`), dùng cho các request API.
  - `refreshToken`: Hiệu lực 7 ngày (`604800000ms`), lưu trong bảng `refresh_token` (quan hệ 1:1 với `users`).
  - Khi gọi `/api/auth/refresh`, refresh token cũ bị thu hồi/xóa và thay thế bằng refresh token mới.
- **Mã hóa Mật khẩu:** Sử dụng `BCryptPasswordEncoder` với salt ngẫu nhiên.
- **Kiểm soát Quyền sở hữu (Ownership Enforcement):** Trong mọi Service (`DebtService`, `PaymentService`, `PlanningService`), hệ thống luôn đối chiếu `userId` của tài nguyên với `userId` của người dùng đã xác thực.

---

## 7. Các Mẫu Thiết kế Áp dụng (Design Patterns)

| Pattern | Vị trí áp dụng | Mục đích |
|---|---|---|
| **Feature-Based Architecture** | Toàn bộ dự án | Đóng gói mã nguồn theo tính năng độc lập, tăng khả năng mở rộng. |
| **Strategy Pattern** | `InterestCalculationStrategy` (`Flat`, `ReducingBalance`), `DebtSelectionStrategy` (`MinimizeInterest`, `ImproveCashflow`) | Cho phép hoán đổi linh hoạt các thuật toán tính lãi và chiến lược trả nợ khi chạy. |
| **Factory Pattern** | `InterestCalculationStrategyFactory` | Đóng gói logic khởi tạo đối tượng tính lãi dựa trên `InterestCalculationMethod`. |
| **Observer / Event Pattern** | `EventPublisher`, `PaymentCompletedEvent`, `PaymentReminderEvent`, Event Handlers | Tách rời nghiệp vụ thanh toán/nhắc nợ với nghiệp vụ tạo thông báo. |
| **Simulation Engine Pattern** | `SimulationEngine` | Thực thi mô phỏng hàng tháng trên bản sao in-memory (`DebtSnapshot`), không làm ảnh hưởng DB. |
| **Soft Delete Pattern** | `Debt.deleted`, `Payment.deleted`, `Notification.deleted` | Bảo toàn dữ liệu lịch sử tài chính cho báo cáo thống kê. |
| **DTO & Mapper Pattern** | Mọi Controller ↔ Service boundary | Tách biệt cấu trúc dữ liệu giao tiếp API với Entity Database. |
| **Scheduler Pattern** | `DebtScheduler`, `PaymentReminderScheduler` | Tự động hóa các tác vụ định kỳ hàng ngày (tính lãi, quét hạn nợ). |

---

## 8. Danh mục REST API (API Catalog)

Tất cả các endpoint (ngoại trừ `/api/auth/**`) đều yêu cầu Header: `Authorization: Bearer <accessToken>`.

### 8.1 Authentication & User Management
| Method | Endpoint | Mô tả |
|---|---|---|
| POST | `/api/auth/register` | Đăng ký tài khoản người dùng mới |
| POST | `/api/auth/login` | Đăng nhập hệ thống, nhận cặp Access + Refresh token |
| POST | `/api/auth/refresh` | Làm mới Access token bằng Refresh token (Token rotation) |
| POST | `/api/auth/logout` | Đăng xuất, hủy Refresh token hiện hành |
| GET | `/api/users/me` | Lấy thông tin tài khoản cá nhân hiện tại |
| PUT | `/api/users/me` | Cập nhật thông tin cá nhân (họ tên) |
| POST | `/api/users/change-password` | Đổi mật khẩu tài khoản |

### 8.2 Finance Profile
| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/finance-profile` | Lấy thông tin hồ sơ tài chính (thu nhập, chi phí thiết yếu) |
| PUT | `/api/finance-profile` | Cập nhật thu nhập và chi phí thiết yếu hàng tháng |

### 8.3 Debt Management
| Method | Endpoint | Mô tả |
|---|---|---|
| POST | `/api/debts` | Tạo khoản nợ mới (tự động tính khoản trả hàng tháng) |
| GET | `/api/debts` | Lấy danh sách nợ (hỗ trợ lọc `search`, `status`, `interestMethod`, sắp xếp) |
| GET | `/api/debts/{id}` | Lấy chi tiết thông tin một khoản nợ |
| PUT | `/api/debts/{id}` | Cập nhật thông tin khoản nợ (tên bên cho vay) |
| DELETE | `/api/debts/{id}` | Xóa mềm khoản nợ (`deleted = true`) |
| GET | `/api/debts/{debtId}/payments` | Lấy danh sách lịch sử thanh toán của riêng khoản nợ này |

### 8.4 Payment Management
| Method | Endpoint | Mô tả |
|---|---|---|
| POST | `/api/payments` | Ghi nhận thanh toán (áp dụng phân bổ Interest-First, phát event) |
| GET | `/api/payments` | Lấy danh sách toàn bộ lịch sử thanh toán (phân trang, lọc ngày) |
| GET | `/api/payments/{id}` | Lấy chi tiết một giao dịch thanh toán |

### 8.5 Repayment Planning & Simulation
| Method | Endpoint | Mô tả |
|---|---|---|
| POST | `/api/planning/compare` | Mô phỏng và so sánh 2 chiến lược trả nợ (stateless, không lưu DB) |
| POST | `/api/planning/save` | Lưu kế hoạch trả nợ đã chọn kèm lịch trình chi tiết |
| GET | `/api/planning/saved` | Lấy thông tin kế hoạch trả nợ đã lưu của người dùng |
| DELETE | `/api/planning/saved` | Xóa kế hoạch trả nợ đã lưu và các bảng chi tiết con |

### 8.6 Financial Analysis & Dashboard
| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/analysis/all` | Phân tích 4 chỉ số sức khỏe tài chính (DTI, Lãi vay, Quá hạn, Thời gian) |
| GET | `/api/dashboard` | Lấy dữ liệu thống kê tổng quan (dư nợ, lãi phát sinh, nợ sắp đến hạn) |

### 8.7 Notifications
| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/notifications` | Lấy danh sách thông báo của người dùng |
| PATCH | `/api/notifications/{id}/read` | Đánh dấu đã đọc một thông báo cụ thể |
| PATCH | `/api/notifications/read-all` | Đánh dấu đã đọc toàn bộ thông báo |

---

## 9. Kiến trúc Triển khai (Deployment Overview)

Hệ thống được triển khai trên nền tảng điện toán đám mây **Amazon Web Services (AWS)** với mô hình mạng VPC cách ly:
- **Backend Application:** Spring Boot chạy trên máy chủ **Amazon EC2 (Ubuntu)** trong Public Subnet, quản lý tiến trình bằng Linux `systemd`.
- **Database:** **Amazon RDS PostgreSQL** đặt trong Private Subnet đa vùng sẵn sàng (Multi-AZ DB Subnet Group), chỉ chấp nhận kết nối nội bộ từ EC2 Security Group.

> Chi tiết cấu hình hạ tầng, biến môi trường và quy trình vận hành được trình bày tại [DEPLOYMENT.md](DEPLOYMENT.md).
