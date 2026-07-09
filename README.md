# DebtWizard

DebtWizard là nền tảng quản lý nợ cá nhân trên nền tảng web, giúp người dùng quản lý các khoản nợ, theo dõi lịch sử thanh toán và xây dựng kế hoạch trả nợ dựa trên phân tích tình hình tài chính.

## Features

- **Quản lý khoản nợ**: tạo, cập nhật và theo dõi nhiều khoản nợ (BANKING, PERSONAL_LOAN, CREDIT).
- **Theo dõi thanh toán**: ghi nhận các khoản thanh toán, theo dõi tiến độ trả nợ theo nguyên tắc interest-first.
- **Tính lãi tự động**: hỗ trợ 2 phương pháp FLAT và REDUCING_BALANCE, accrual hàng ngày qua Scheduler.
- **Dashboard**: tổng hợp thông tin tài chính tổng quan — danh sách khoản nợ, dư nợ gốc, lãi phát sinh, tổng số tiền phải trả.
- **Phân tích tài chính**: đánh giá sức khỏe tài chính qua 4 chỉ số định lượng — DTI, tỷ lệ gánh nặng lãi vay, tỷ lệ nợ quá hạn, thời gian dự kiến trả hết nợ — kèm mức đánh giá và khuyến nghị.
- **Lập kế hoạch trả nợ**: so sánh 2 chiến lược (MinimizeInterest / Improve Cashflow), lưu kế hoạch đã chọn với lịch trình chi tiết từng tháng.
- **Gamification** *(Dự kiến phát triển)*.

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.2.4 |
| Language | Java 17 |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Authentication | JWT (JJWT 0.12.5) |
| Authorization | Spring Security |
| API Docs | SpringDoc OpenAPI 2.5.0 (Swagger UI) |
| Build Tool | Maven |
| Utilities | Lombok, MapStruct 1.5.5 |

## Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/Qt159/DebtWizard.git
cd DebtWizard
```

### 2. Create the database

```sql
CREATE DATABASE debtwizard;
```

### 3. Configure environment variables

**Development (Local):**

Tạo file `.env` tại thư mục gốc của project:

```env
DB_PASSWORD=your_postgres_password
JWT_SECRET=your_jwt_secret_key
```

Sau đó export vào shell trước khi chạy:
```bash
# Linux/macOS
export $(cat .env | xargs)

# Windows PowerShell
Get-Content .env | ForEach-Object { $var = $_.Split('='); [System.Environment]::SetEnvironmentVariable($var[0], $var[1]) }
```

**Production (EC2/Server):**

Không cần `.env` file. Set environment variables trực tiếp:

```bash
# Option 1: Export before running
export DB_PASSWORD=your_password
export JWT_SECRET=your_secret
java -jar DebtWizard.jar

# Option 2: Pass as arguments
java -jar DebtWizard.jar \
  --spring.datasource.password=your_password \
  --jwt.secret=your_secret
  
# Option 3: Use systemd service with Environment= directive (recommended)
```

### 4. Run the application

Nếu đã cài Maven trên máy:

```bash
mvn spring-boot:run
```

Hoặc dùng Maven Wrapper (không cần cài Maven sẵn):

- **Windows CMD:**
  ```cmd
  mvnw.cmd spring-boot:run
  ```
- **Windows PowerShell:**
  ```powershell
  .\mvnw spring-boot:run
  ```
- **Linux / macOS:**
  ```bash
  ./mvnw spring-boot:run
  ```

Server khởi động tại `http://localhost:8080`.

## API Documentation

Swagger UI:

```
http://localhost:8080/swagger-ui/index.html
```

Tất cả các API yêu cầu xác thực đều sử dụng Bearer Token.

1. Đăng nhập qua `POST /api/auth/login` để lấy access token.
2. Nhấn **Authorize** trên Swagger UI và nhập `Bearer <token>`.

## Project Structure

```
src/main/java/com/tuan/debtwizard/
├── config/          # Security, OpenAPI, và app configuration
├── dto/             # Shared DTOs (ApiResponse)
├── exception/       # Global exception handler và error codes
└── features/
    ├── auth/        # Đăng ký, đăng nhập, JWT filter, refresh token
    ├── user/        # Quản lý profile, đổi mật khẩu
    ├── debt/        # CRUD khoản nợ, tính lãi, quản lý trạng thái
    ├── payment/     # Ghi nhận và theo dõi thanh toán
    ├── planning/    # So sánh kế hoạch trả nợ, lưu kế hoạch đã chọn
    ├── analysis/    # Phân tích 4 chỉ số sức khỏe tài chính
    └── dashboard/   # Tổng hợp thông tin tài chính tổng quan
```

## Environment Variables

| Variable | Description | Required |
|---|---|---|
| `DB_PASSWORD` | Mật khẩu PostgreSQL | Yes |
| `JWT_SECRET` | Secret key để ký JWT token | Yes |

## Documentation

Tài liệu bổ sung trong thư mục `docs/`:

- [System Architecture Document (SAD)](docs/SAD.md)
- [Database Design](docs/DATABASE_DESIGN.md)
- [Business Rules](docs/business_rules.md)
- [Validation Rules](docs/validation_rules.md)
- [Algorithm](docs/algorithm.md)

## Testing the API

Dự án cung cấp sẵn bộ sưu tập Postman:

- `DebtWizard.postman_collection.json`
- `DebtWizard.postman_environment.json`

Import cả hai tệp vào Postman để bắt đầu kiểm thử các API.
