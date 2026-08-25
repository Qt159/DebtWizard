# DebtWizard

DebtWizard là nền tảng quản lý nợ cá nhân và hỗ trợ ra quyết định tài chính (Financial Decision Support System), giúp người dùng theo dõi tập trung các khoản nợ, tính toán lãi suất tự động, đánh giá sức khỏe tài chính và xây dựng kế hoạch trả nợ tối ưu dựa trên thu nhập và ngân sách thực tế.

---

## Tính năng chính (Core Features)

- **Quản lý khoản nợ (`debt`):** Khởi tạo, cập nhật và theo dõi nhiều loại khoản nợ (`BANKING`, `PERSONAL_LOAN`, `CREDIT`), hỗ trợ xóa mềm (soft-delete).
- **Tính lãi tự động (`interest`):** Hỗ trợ 2 phương pháp tính lãi chuẩn (`FLAT` và `REDUCING_BALANCE`), tự động cộng dồn lãi phát sinh hàng ngày thông qua `DebtScheduler`.
- **Quản lý thanh toán (`payment`):** Ghi nhận thanh toán thực tế, áp dụng nguyên tắc phân bổ ưu tiên trả lãi trước gốc sau (Interest-First Allocation) và tự động cập nhật trạng thái nợ (`PAID_OFF`, `OVERDUE`, `ACTIVE`).
- **Mô phỏng & Lập kế hoạch trả nợ (`planning`):** Động cơ mô phỏng in-memory 600 tháng so sánh 2 chiến lược (`MINIMIZE_INTEREST` - Avalanche vs `IMPROVE_CASHFLOW`), áp dụng cơ chế giải phóng dòng tiền (Snowball bonus) và lưu trữ kế hoạch chi tiết từng tháng.
- **Phân tích sức khỏe tài chính (`analysis`):** Đánh giá định lượng qua 4 chỉ số tài chính — DTI (Debt-to-Income), tỷ lệ gánh nặng lãi vay, tỷ lệ nợ quá hạn và thời gian dự kiến sạch nợ.
- **Hồ sơ tài chính (`financeprofile`):** Quản lý thu nhập và chi phí thiết yếu hàng tháng làm cơ sở xác thực ngân sách trả thêm tối đa.
- **Bảng điều khiển tổng quan (`dashboard`):** Tổng hợp dữ liệu thời gian thực gồm tổng dư nợ gốc, lãi lũy kế, tổng nghĩa vụ nợ hàng tháng và danh sách nợ sắp đến hạn.
- **Thông báo & Nhắc nợ (`notification` & `event`):** Kiến trúc hướng sự kiện (Event-Driven) tự động tạo thông báo xác nhận thanh toán và nhắc nợ trước 3 ngày.

---

## Công nghệ sử dụng (Tech Stack)

| Thành phần | Công nghệ |
|---|---|
| **Backend Framework** | Spring Boot 3.2.4 |
| **Ngôn ngữ** | Java 17 |
| **Cơ sở dữ liệu** | PostgreSQL 14+ |
| **ORM / Data Access** | Spring Data JPA / Hibernate ORM |
| **Xác thực & Bảo mật** | Spring Security, JJWT 0.12.5 (Access Token + Refresh Token Rotation), BCrypt |
| **Tài liệu API** | SpringDoc OpenAPI 2.5.0 (Swagger UI) |
| **Build Tool** | Apache Maven |
| **Hạ tầng Cloud** | AWS EC2 (Ubuntu), Amazon RDS PostgreSQL (Multi-AZ DB Subnets), VPC |

---

## Cấu trúc Mã nguồn (Project Structure)

```text
src/main/java/com/tuan/debtwizard/
├── config/              # Security, OpenAPI Swagger, Web config
├── dto/                 # Generic ApiResponse<T>, Global shared DTOs
├── exception/           # Global exception handler & Business error codes
└── features/
    ├── auth/            # Authentication, JWT generation, Refresh token rotation
    ├── user/            # User profile management & Password change
    ├── financeprofile/  # Monthly income & Essential expenses profile
    ├── debt/            # Debt management, Interest engines (Flat/Reducing), Scheduler
    ├── payment/         # Payment tracking & Interest-first allocation
    ├── planning/        # SimulationEngine, Repayment strategies, Plan persistence
    ├── analysis/        # 4 Financial health indicators & Classification
    ├── dashboard/       # Financial overview metrics aggregation
    ├── notification/    # In-app notifications & Payment reminder scheduler
    └── event/           # Event Publisher & Domain Events (PaymentCompleted, PaymentReminder)
```

---

## Hướng dẫn Cài đặt & Chạy ứng dụng (Getting Started)

### 1. Yêu cầu hệ thống (Prerequisites)
- Git
- Java 17 (JDK)
- Apache Maven 3.8+
- PostgreSQL 14+

### 2. Clone mã nguồn
```bash
git clone https://github.com/Qt159/DebtWizard.git
cd DebtWizard
```

### 3. Khởi tạo Cơ sở dữ liệu
```sql
CREATE DATABASE debtwizard;
```

### 4. Cấu hình Biến môi trường
Tạo file `.env` tại thư mục gốc của dự án:
```properties
# Database Configuration
DB_HOST=localhost                # Default: localhost
DB_PORT=5432                     # Default: 5432
DB_NAME=debtwizard               # Default: debtwizard
DB_USERNAME=postgres             # Default: postgres
DB_PASSWORD=your_postgres_password

# JWT Configuration
JWT_SECRET=your_jwt_secret_key_at_least_32_chars_long_123456
JWT_ACCESS_EXPIRATION=900000     # Default: 15 phút (900.000 ms)
JWT_REFRESH_EXPIRATION=604800000 # Default: 7 ngày (604.800.000 ms)
```

### 5. Chạy ứng dụng
```bash
mvn spring-boot:run
```
Ứng dụng sẽ khởi chạy tại: `http://localhost:8080`

---

## Tài liệu API & Kiểm thử (API Documentation & Testing)

- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **Quy trình xác thực trên Swagger:**
  1. Gửi request `POST /api/auth/login` để nhận `accessToken`.
  2. Nhấn nút **Authorize** tại góc trên bên phải Swagger UI.
  3. Nhập giá trị: `Bearer <access-token>` và nhấn Xác nhận.
- **Postman Collections:** Dự án cung cấp sẵn collection và environment mẫu tại thư mục `postman/`:
  - `postman/DebtWizard.postman_collection.json`
  - `postman/DebtWizard.postman_environment.json`

---

## Hệ thống Tài liệu Dự án (Project Documentation)

Bộ tài liệu kiến trúc và thiết kế của dự án được chuẩn hóa và quản lý tại thư mục `docs/`:

| Tài liệu | Mô tả chi tiết |
|---|---|
| [**SRS (Software Requirements Specification)**](docs/SRS.md) | Đặc tả toàn diện yêu cầu chức năng, yêu cầu phi chức năng, quy tắc nghiệp vụ (BR01–BR09), kiểm thực dữ liệu (VR01–VR31) và bảng mã lỗi hệ thống. |
| [**SAD (Software Architecture Document)**](docs/SAD.md) | Kiến trúc hệ thống tổng thể, mô hình Feature-based, thiết kế Event-driven (Spring Events & AWS SQS/Lambda), bảo mật JWT Token Rotation, Design Patterns và REST API Catalog. |
| [**Database Design**](docs/DATABASE_DESIGN.md) | Sơ đồ quan hệ thực thể (ERD), cấu trúc chi tiết 9 bảng dữ liệu, ràng buộc khóa ngoại, cơ chế Embedded `InterestSettings`, chỉ mục và chính sách Cascade / Soft delete. |
| [**Deployment Guide**](docs/DEPLOYMENT.md) | Hướng dẫn chi tiết thiết lập hạ tầng AWS (VPC, Public/Private Subnets, EC2, Amazon RDS PostgreSQL Multi-AZ), cấu hình biến môi trường, quản lý dịch vụ nền bằng Linux `systemd` và CI/CD. |
| [**Planning & Simulation Engine**](docs/PLANNING_SIMULATION.md) | Tài liệu chuyên sâu về động cơ mô phỏng: chiến lược `MINIMIZE_INTEREST` vs `IMPROVE_CASHFLOW`, thuật toán vòng lặp hàng tháng, công thức tính lãi Flat/Amortization, ngân sách Extra Payment và cơ chế Snowball bonus. |
