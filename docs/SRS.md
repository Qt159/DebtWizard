# Software Requirements Specification (SRS) — DebtWizard

---

## 1. Giới thiệu & Tổng quan sản phẩm

### 1.1 Mục tiêu hệ thống (Product Overview)
**DebtWizard** là nền tảng quản lý nợ cá nhân và hỗ trợ đưa ra quyết định tài chính (Financial Decision Support System). Hệ thống giúp người dùng theo dõi tập trung các khoản nợ, tính toán lãi suất tự động, phân tích các chỉ số sức khỏe tài chính định lượng, đồng thời mô phỏng và so sánh các chiến lược trả nợ tối ưu dựa trên thu nhập và ngân sách thực tế.

> **Tôn chỉ cốt lõi:** *"DebtWizard supports financial decisions — it does not make them for the user."* (Hệ thống cung cấp dữ liệu, phân tích và các kịch bản đánh đổi (trade-offs) để người dùng tự chủ đưa ra quyết định phù hợp nhất với hoàn cảnh của mình).

### 1.2 Vấn đề giải quyết (Problem Statement)
Người có nhiều khoản nợ thường đối mặt với các khó khăn:
- Khó theo dõi phân tán nhiều khoản nợ với các mức lãi suất, kỳ hạn, phương pháp tính lãi và ngày đến hạn khác nhau.
- Không biết nên ưu tiên dồn tiền trả khoản nợ nào trước để tiết kiệm tiền lãi hoặc giảm áp lực dòng tiền.
- Không biết cách sử dụng khoản tiền nhàn rỗi (extra payment) sao cho tối ưu hiệu quả tài chính.
- Thiếu công cụ ước tính chính xác tổng số tiền lãi phải trả và thời gian cụ thể để sạch nợ (debt-free).
- Chưa có góc nhìn định lượng về mức độ an toàn tài chính của bản thân (DTI, gánh nặng lãi vay, tỷ lệ quá hạn).

### 1.3 Đối tượng người dùng mục tiêu (Target User)
Cá nhân đang có từ một hoặc nhiều khoản nợ (ngân hàng, vay tiêu dùng, thẻ tín dụng), có nguồn thu nhập ổn định đủ khả năng thanh toán khoản tối thiểu hàng tháng và mong muốn chủ động lập kế hoạch dứt điểm nợ vay.

### 1.4 Giá trị cốt lõi (Core Values)
1. **Manage (Quản lý):** Theo dõi chi tiết thông tin từng khoản nợ, phương pháp tính lãi, lịch sử thanh toán và trạng thái nợ (Active, Overdue, Paid Off).
2. **Understand (Thấu hiểu):** Cung cấp bức tranh toàn cảnh về dư nợ gốc, lãi lũy kế và 4 chỉ số sức khỏe tài chính định lượng (DTI, Interest Ratio, Overdue Ratio, Repayment Timeline).
3. **Decide (Quyết định):** Mô phỏng độc lập các chiến lược trả nợ (Avalanche vs Improve Cashflow), trực quan hóa bảng phân bổ từng tháng và lưu kế hoạch đã chọn.

### 1.5 Phạm vi ngoài mục tiêu (Non-Goals)
DebtWizard **không**:
- Cung cấp dịch vụ cho vay hoặc trung gian tín dụng.
- Thực hiện giao dịch chuyển khoản / thanh toán tiền tệ thực tế.
- Kết nối Open Banking trực tiếp hoặc quản trị tài khoản ngân hàng.
- Cung cấp lời khuyên tư vấn tài chính pháp lý chuyên nghiệp.
- Mở rộng thành siêu ứng dụng quản lý đầu tư / tài sản (Wealth Management).

---

## 2. Đối tượng & Phân quyền người dùng

| Vai trò (Role) | Mô tả | Quyền hạn |
|---|---|---|
| **Guest (Chưa đăng nhập)** | Khách truy cập hệ thống | Đăng ký tài khoản mới (`/api/auth/register`), Đăng nhập (`/api/auth/login`), Refresh Token (`/api/auth/refresh`), Xem tài liệu Swagger API. |
| **Authenticated User** | Người dùng đã xác thực qua JWT | Toàn quyền quản lý hồ sơ cá nhân, hồ sơ tài chính, danh sách nợ, lịch sử thanh toán, chạy mô phỏng kế hoạch và nhận thông báo của chính mình. |

> **Nguyên tắc cô lập dữ liệu (Data Isolation):** Toàn bộ tài nguyên (khoản nợ, thanh toán, hồ sơ tài chính, kế hoạch, thông báo) gắn liền với `userId`. Hệ thống bắt buộc kiểm tra quyền sở hữu (Ownership Check) trên từng tác vụ; người dùng tuyệt đối không thể truy cập hoặc thao tác trên dữ liệu của người dùng khác.

---

## 3. Quy trình người dùng tổng thể (Core Workflow)

```text
[Đăng ký / Đăng nhập] 
         ↓
[Cập nhật Hồ sơ Tài chính (Thu nhập & Chi phí thiết yếu)]
         ↓
[Khai báo & Quản lý Danh sách Khoản nợ]
         ↓
[Theo dõi Tổng quan qua Dashboard & Phân tích Sức khỏe Tài chính]
         ↓
[Mô phỏng & So sánh Kế hoạch Trả nợ (Chọn Extra Payment & Chiến lược)]
         ↓
[Lưu Kế hoạch tối ưu đã chọn]
         ↓
[Ghi nhận Thanh toán thực tế theo tiến trình (Interest-First)]
         ↓
[Nhận Thông báo & Theo dõi tiến độ hoàn thành sạch nợ]
```

---

## 4. Yêu cầu chức năng chi tiết (Functional Requirements)

### 4.1 Module Quản lý Xác thực & Người dùng (`auth` & `user`)
- **FR-AUTH-01 (Đăng ký tài khoản):** Cho phép người dùng đăng ký tài khoản với `username`, `password`, `fullName`, `email`. Hệ thống tự động khởi tạo bản ghi `FinanceProfile` mặc định. Mật khẩu được mã hóa an toàn bằng BCrypt.
- **FR-AUTH-02 (Đăng nhập):** Xác thực tài khoản, trả về cặp JWT tokens: `accessToken` (thời hạn 15 phút) và `refreshToken` (thời hạn 7 ngày).
- **FR-AUTH-03 (Làm mới Token):** Cho phép sử dụng `refreshToken` còn hạn để nhận cặp token mới; áp dụng cơ chế Token Rotation (xóa token cũ, tạo token mới).
- **FR-AUTH-04 (Đăng xuất):** Hủy `refreshToken` của người dùng trong cơ sở dữ liệu.
- **FR-USER-01 (Xem hồ sơ):** Lấy thông tin tài khoản cá nhân của người dùng hiện tại (`/api/users/me`).
- **FR-USER-02 (Cập nhật hồ sơ):** Cho phép cập nhật `fullName`, `email`.
- **FR-USER-03 (Đổi mật khẩu):** Xác thực mật khẩu cũ và cập nhật mật khẩu mới theo chuẩn bảo mật.

### 4.2 Module Hồ sơ Tài chính (`financeprofile`)
- **FR-FP-01 (Xem hồ sơ tài chính):** Lấy thông tin thu nhập hàng tháng (`monthlyIncome`) và chi phí thiết yếu hàng tháng (`monthlyEssentialExpenses`).
- **FR-FP-02 (Cập nhật hồ sơ tài chính):** Cập nhật thu nhập và chi phí sinh hoạt thiết yếu để làm cơ sở tính toán DTI, Interest Ratio và ngưỡng Extra Payment tối đa cho mô phỏng trả nợ.

### 4.3 Module Quản lý Khoản nợ (`debt`)
- **FR-DEBT-01 (Tạo khoản nợ):** Tạo khoản nợ mới gồm: Tên bên cho vay (`lenderName`), Nợ gốc ban đầu (`totalPrincipal`), Kỳ hạn vay (`termMonths`), Ngày bắt đầu (`startDate`), Ngày đến hạn hàng tháng (`dueDay`), Loại nợ (`debtType`: `BANKING`, `PERSONAL_LOAN`, `CREDIT`), và Cấu hình lãi suất (`InterestSettings`: `FLAT` hoặc `REDUCING_BALANCE`, tần suất `DAILY`/`MONTHLY`/`ANNUALLY`, lãi suất năm `%`).
- **FR-DEBT-02 (Tự động tính thanh toán hàng tháng):** Khi tạo nợ, hệ thống tự động tính `expectedMonthlyPayment` theo công thức tương ứng với phương pháp tính lãi (Flat Amortization hoặc Reducing Balance Amortization) và xác định `nextDueDate` đầu tiên.
- **FR-DEBT-03 (Xem danh sách nợ):** Lấy danh sách khoản nợ với các bộ lọc (`search`, `status`: `ACTIVE`, `OVERDUE`, `PAID_OFF`, `interestMethod`) và hỗ trợ sắp xếp (`sortBy`, `sortDir`).
- **FR-DEBT-04 (Xem chi tiết nợ):** Xem đầy đủ thông tin một khoản nợ kèm tổng dư nợ (`totalOutstanding = remainingPrincipal + accruedInterest`).
- **FR-DEBT-05 (Cập nhật khoản nợ):** Chỉ cho phép chỉnh sửa thông tin mô tả phi tài chính (`lenderName`). Các tham số tài chính gốc không được chỉnh sửa sau khi tạo nhằm đảm bảo tính toàn vẹn lịch sử.
- **FR-DEBT-06 (Xóa khoản nợ):** Thực hiện xóa mềm (`deleted = true`), giữ nguyên tính toàn vẹn dữ liệu cho các báo cáo lịch sử.
- **FR-DEBT-07 (Tự động cộng dồn lãi & cập nhật trạng thái hàng ngày):** Chạy Scheduler định kỳ vào `00:00` hàng ngày để:
  - Tính lãi phát sinh từ `lastInterestAccruedDate` đến ngày hiện tại và cộng vào `accruedInterest`.
  - Tự động chuyển trạng thái sang `OVERDUE` nếu ngày hiện tại vượt quá `nextDueDate`, hoặc `PAID_OFF` nếu tổng dư nợ `<= 0`.

### 4.4 Module Quản lý Thanh toán (`payment`)
- **FR-PAY-01 (Ghi nhận thanh toán):** Cho phép người dùng nhập thông tin thanh toán: `debtId`, `amount`, `paymentDate`, `paymentMethod` (`CASH`, `BANK_TRANSFER`, `E_WALLET`, `CREDIT_CARD`), và `note`.
- **FR-PAY-02 (Nguyên tắc Interest-First Allocation):**
  - Hệ thống tự động tính và cộng dồn lãi phát sinh đến ngày thanh toán (`paymentDate`).
  - Số tiền thanh toán được ưu tiên trừ hết `accruedInterest` trước, số tiền còn lại mới trừ vào `remainingPrincipal`.
  - Nếu số tiền thanh toán >= `expectedMonthlyPayment`, tự động dời `nextDueDate` sang kỳ thanh toán tháng tiếp theo.
  - Nếu dư nợ về 0, tự động cập nhật trạng thái nợ thành `PAID_OFF` và lưu `paidOffAt`.
- **FR-PAY-03 (Xem lịch sử thanh toán):** Hỗ trợ phân trang (`page`, `pageSize`), lọc theo khoảng ngày (`dateFrom`, `dateTo`), sắp xếp và xem theo từng khoản nợ cụ thể hoặc toàn bộ hệ thống.
- **FR-PAY-04 (Xem chi tiết giao dịch):** Lấy thông tin chi tiết một lần thanh toán kèm số tiền trả gốc (`principalPaid`) và trả lãi (`interestPaid`).
- **FR-PAY-05 (Phát sinh Event thanh toán):** Sau khi ghi nhận thanh toán thành công, hệ thống tự động phát ra `PaymentCompletedEvent` để xử lý thông báo bất đồng bộ.

### 4.5 Module Kế hoạch & Mô phỏng Trả nợ (`planning`)
- **FR-PLAN-01 (Mô phỏng & So sánh kế hoạch - Stateless):**
  - Nhận danh sách các khoản nợ cần lập kế hoạch (`debtIds`), số tiền trả thêm (`monthlyExtraPayment`), và 2 chiến lược cần so sánh (`firstStrategy`, `secondStrategy`).
  - Kiểm tra và xác thực ngân sách trả thêm: `monthlyExtraPayment <= maxAllowedExtraPayment` (trong đó `maxAllowedExtraPayment = monthlyIncome - monthlyEssentialExpenses - totalMinimumPayment`).
  - Chạy mô phỏng vòng lặp độc lập trên bản sao `DebtSnapshot` cho 2 chiến lược:
    - `MINIMIZE_INTEREST` (Avalanche): Ưu tiên dồn extra payment vào khoản nợ có lãi suất cao nhất.
    - `IMPROVE_CASHFLOW`: Ưu tiên dồn extra payment vào khoản nợ có điểm giải phóng dòng tiền cao nhất (`score = minimumPayment^2 / balance`).
  - Trả về so sánh chi tiết: Tổng lãi phải trả, thời gian sạch nợ (tháng), và bảng lịch thanh toán từng tháng của cả 2 kế hoạch mà không lưu vào cơ sở dữ liệu.
- **FR-PLAN-02 (Lưu kế hoạch trả nợ):**
  - Cho phép người dùng lưu lại kế hoạch trả nợ đã chọn cùng lịch trình chi tiết từng tháng (`plan_monthly_schedules`) và phân bổ chi tiết cho từng khoản nợ (`plan_debt_payments`).
  - Mỗi người dùng chỉ duy trì tối đa 1 kế hoạch hiện hành (Lưu kế hoạch mới sẽ tự động ghi đè/xóa kế hoạch cũ).
- **FR-PLAN-03 (Xem kế hoạch đã lưu):** Lấy thông tin kế hoạch trả nợ hiện tại kèm toàn bộ lịch thanh toán theo từng tháng.
- **FR-PLAN-04 (Xóa kế hoạch đã lưu):** Cho phép người dùng xóa kế hoạch hiện hành và các bảng con liên quan (Cascade Delete).

### 4.6 Module Phân tích Sức khỏe Tài chính (`analysis`)
- **FR-ANA-01 (Phân tích toàn diện):** Tính toán và phân loại tình trạng sức khỏe tài chính dựa trên 4 chỉ số định lượng:
  1. **DTI (Debt-to-Income):** `(Tổng số tiền trả nợ hàng tháng / Thu nhập hàng tháng) * 100`.
     - `< 30%`: `GOOD` (An toàn)
     - `30% - 50%`: `WARNING` (Cảnh báo)
     - `> 50%`: `CRITICAL` (Nguy hiểm)
  2. **Interest Burden Ratio:** `(Tổng tiền lãi ước tính hàng tháng / Thu nhập hàng tháng) * 100`.
     - `< 10%`: `GOOD`
     - `10% - 20%`: `WARNING`
     - `> 20%`: `CRITICAL`
  3. **Overdue Debt Ratio:** `(Số khoản nợ quá hạn / Tổng số khoản nợ đang hoạt động) * 100`.
     - `< 30%`: `GOOD`
     - `30% - 50%`: `WARNING`
     - `> 50%`: `CRITICAL`
  4. **Estimated Repayment Timeline:** `Tổng dư nợ còn lại / Tổng số tiền trả nợ hàng tháng` (số tháng dự kiến để hoàn tất nếu chỉ trả mức tối thiểu).
- **FR-ANA-02 (Khuyến nghị tài chính):** Trả về lời khuyên tương ứng với từng mức độ sức khỏe tài chính (`GOOD`, `WARNING`, `CRITICAL`, `INCOMPLETE`).

### 4.7 Module Bảng điều khiển Tổng quan (`dashboard`)
- **FR-DASH-01 (Thống kê tổng quan):** Tổng hợp dữ liệu tài chính thời gian thực:
  - Tổng số khoản nợ (Active, Overdue, Paid off).
  - Tổng dư nợ gốc (`totalRemainingPrincipal`), tổng lãi lũy kế (`totalAccruedInterest`), tổng nghĩa vụ nợ (`totalOutstandingDebt`).
  - Tổng nghĩa vụ thanh toán hàng tháng (`totalMonthlyPayment`).
  - Danh sách các khoản nợ sắp đến hạn thanh toán trong tháng.

### 4.8 Module Thông báo & Nhắc nợ (`notification` & `event`)
- **FR-NOTI-01 (Thông báo thanh toán thành công):** Lắng nghe `PaymentCompletedEvent` và tự động tạo thông báo xác nhận thanh toán kèm số tiền và tên khoản nợ.
- **FR-NOTI-02 (Nhắc nợ tự động định kỳ):** Chạy Scheduler hàng ngày quét các khoản nợ có `nextDueDate = ngày hiện tại + 3 ngày`, phát ra `PaymentReminderEvent` để tạo thông báo nhắc nhở người dùng trước 3 ngày.
- **FR-NOTI-03 (Xem danh sách thông báo):** Lấy danh sách thông báo của người dùng kèm trạng thái đã đọc (`isRead`).
- **FR-NOTI-04 (Đánh dấu đã đọc):** Cho phép đánh dấu đã đọc một thông báo cụ thể hoặc đánh dấu đọc toàn bộ thông báo (`/read-all`).
- **FR-NOTI-05 (Chống trùng lặp - Idempotency):** Áp dụng khóa duy nhất `referenceKey` trên bảng `notifications` để đảm bảo không tạo trùng thông báo ngay cả khi nhận lại event nhiều lần.

---

## 5. Quy tắc Nghiệp vụ (Business Rules)

- **BR01 (Thanh toán tối thiểu trong Simulation):** Trong mỗi tháng mô phỏng, hệ thống bắt buộc phân bổ thanh toán `minimumPayment` cho tất cả các khoản nợ còn `balance > 0` trước khi phân bổ tiền trả thêm (`extraPayment`). Nếu `minimumPayment > balance`, giá trị thanh toán được giới hạn bằng `balance`.
- **BR02 (Ngân sách Extra Payment):** Người dùng chỉ được nhập `monthlyExtraPayment` trong phạm vi cho phép: `0 <= monthlyExtraPayment <= (monthlyIncome - monthlyEssentialExpenses - totalMinimumPayment)`. Nếu vượt quá ngân sách, hệ thống từ chối và trả mã lỗi `EXTRA_PAYMENT_EXCEEDS_BUDGET`.
- **BR03 (Phân bổ lãi trước - Interest First):** Khi ghi nhận thanh toán thực tế, tiền thanh toán luôn trừ hết phần lãi phát sinh lũy kế (`accruedInterest`) trước, số tiền còn lại mới được giảm trừ vào nợ gốc (`remainingPrincipal`).
- **BR04 (Tất toán khoản nợ - Debt Closure):** Khoản nợ được coi là tất toán khi tổng dư nợ (`totalOutstanding <= 0` trong thực tế, hoặc `balance <= 0` trong simulation). Khi đó khoản nợ chuyển trạng thái sang `PAID_OFF` và ghi nhận thời điểm hoàn tất.
- **BR05 (Cơ chế Snowball & Giải phóng Dòng tiền - Cashflow Release):** Khi một khoản nợ được tất toán trong simulation, phần `minimumPayment` của khoản nợ đó được tự động giải phóng và cộng dồn vào ngân sách trả thêm (`snowballBonus`) cho các tháng tiếp theo, tạo hiệu ứng hòn tuyết lăn đẩy nhanh tiến độ trả các khoản nợ còn lại.
- **BR06 (Cộng lãi định kỳ trong Simulation):** Tại mỗi tháng mô phỏng, tiền lãi của tháng được tính dựa trên số dư đầu kỳ: `interest = balance * (interestRate / 100 / 12)` và được cộng vào `balance` trước khi thực hiện các khoản thanh toán.
- **BR07 (Độc lập trong Mô phỏng):** Hai chiến lược so sánh chạy hoàn toàn độc lập trên hai bản sao bộ nhớ riêng biệt (`DebtSnapshot`). Kết quả của chiến lược này không làm thay đổi trạng thái của chiến lược kia.
- **BR08 (Quyền quyết định của Người dùng):** Hệ thống không tự ý áp đặt kế hoạch cho người dùng mà trình bày dữ liệu so sánh trực quan để người dùng tự chủ chọn lưu kế hoạch theo mong muốn.
- **BR09 (Giới hạn an toàn Mô phỏng):** Vòng lặp mô phỏng tự động dừng nếu vượt quá 600 tháng (50 năm) nhằm ngăn chặn hiện tượng lặp vô hạn do dữ liệu đầu vào không hợp lệ (ví dụ: lãi suất quá cao nhưng tiền trả hàng tháng quá nhỏ không đủ bù lãi).

---

## 6. Quy tắc Kiểm thực Dữ liệu (Validation Rules)

### 6.1 Xác thực Tài khoản & Người dùng
- **VR01 (Username):** Bắt buộc, không chứa khoảng trắng, độ dài từ 3 đến 50 ký tự, không được trùng lặp trong hệ thống.
- **VR02 (Password):** Bắt buộc, độ dài tối thiểu 8 ký tự khi đăng ký và đăng nhập.
- **VR03 (Full Name):** Bắt buộc, không để trống, độ dài tối đa 100 ký tự.
- **VR04 (Email):** Phải đúng định dạng chuẩn RFC 5322 email (nếu cung cấp) và không được trùng lặp.
- **VR05 (Monthly Income & Expenses):** Giá trị số >= 0, định dạng tối đa 15 chữ số phần nguyên và 2 chữ số phần thập phân.

### 6.2 Xác thực Khoản nợ
- **VR06 (Lender Name):** Bắt buộc, không để trống, độ dài tối đa 100 ký tự.
- **VR07 (Total Principal):** Bắt buộc, số tiền nợ gốc ban đầu phải > 0.
- **VR08 (Start Date):** Bắt buộc, định dạng ngày hợp lệ `YYYY-MM-DD`.
- **VR09 (Term Months):** Bắt buộc, số nguyên dương > 0.
- **VR10 (Due Day):** Bắt buộc, số nguyên nằm trong khoảng từ 1 đến 31.
- **VR11 (Debt Type):** Bắt buộc, thuộc danh mục: `BANKING`, `PERSONAL_LOAN`, `CREDIT`.
- **VR12 (Interest Method):** Bắt buộc, thuộc danh mục: `FLAT`, `REDUCING_BALANCE`.
- **VR13 (Interest Frequency):** Bắt buộc, thuộc danh mục: `DAILY`, `MONTHLY`, `ANNUALLY`.
- **VR14 (Interest Rate):** Bắt buộc, tỷ lệ lãi suất năm từ 0.00% đến 100.00%.

### 6.3 Xác thực Thanh toán thực tế
- **VR15 (Debt ID):** Bắt buộc, khoản nợ phải tồn tại, thuộc quyền sở hữu của người dùng hiện tại và chưa ở trạng thái `PAID_OFF`.
- **VR16 (Payment Amount):** Bắt buộc, số tiền thanh toán phải > 0 và không vượt quá tổng dư nợ còn lại (`totalOutstanding`).
- **VR17 (Payment Date):** Bắt buộc, không được lớn hơn ngày hiện tại (không ghi nhận thanh toán trong tương lai).
- **VR18 (Payment Method):** Bắt buộc, thuộc danh mục: `CASH`, `BANK_TRANSFER`, `E_WALLET`, `CREDIT_CARD`.
- **VR19 (Payment Note):** Tùy chọn, độ dài tối đa 255 ký tự.

### 6.4 Xác thực Kế hoạch & Mô phỏng
- **VR20 (Debt Selection):** Danh sách `debtIds` không được rỗng, không chứa phần tử trùng lặp (`DUPLICATE_DEBT`), các khoản nợ phải thuộc quyền sở hữu của người dùng và đang ở trạng thái `ACTIVE` hoặc `OVERDUE`.
- **VR21 (Monthly Extra Payment):** Bắt buộc, giá trị >= 0 và không được vượt quá ngân sách khả dụng (`EXTRA_PAYMENT_EXCEEDS_BUDGET`).
- **VR22 (Repayment Strategy Selection):** Bắt buộc; khi gọi so sánh (`/compare`), `firstStrategy` và `secondStrategy` không được trùng nhau (`STRATEGY_DUPLICATE`).

---

## 7. Bảng Mã lỗi Hệ thống (Error Codes)

| Error Code | HTTP Status | Thông điệp / Mô tả |
|---|---|---|
| `USER_NOT_FOUND` | 404 | Không tìm thấy người dùng trong hệ thống. |
| `USERNAME_ALREADY_EXISTS` | 409 | Tên đăng nhập đã được sử dụng. |
| `EMAIL_ALREADY_EXISTS` | 409 | Địa chỉ email đã được sử dụng. |
| `INVALID_CREDENTIALS` | 401 | Tên đăng nhập hoặc mật khẩu không chính xác. |
| `UNAUTHENTICATED` | 401 | Yêu cầu chưa được xác thực hoặc token không hợp lệ. |
| `UNAUTHORIZED` | 403 | Không có quyền thao tác trên tài nguyên của người dùng khác. |
| `REFRESH_TOKEN_EXPIRED` | 401 | Refresh token đã hết hạn, vui lòng đăng nhập lại. |
| `INVALID_REFRESH_TOKEN` | 401 | Refresh token không hợp lệ hoặc đã bị thu hồi. |
| `FINANCE_PROFILE_NOT_FOUND` | 404 | Chưa tìm thấy thông tin hồ sơ tài chính của người dùng. |
| `DEBT_NOT_FOUND` | 404 | Khoản nợ không tồn tại hoặc đã bị xóa. |
| `DEBT_ALREADY_PAID_OFF` | 400 | Khoản nợ đã được tất toán, không thể thao tác thêm. |
| `PAYMENT_EXCEEDS_DEBT_BALANCE`| 400 | Số tiền thanh toán vượt quá tổng dư nợ hiện tại. |
| `PAYMENT_NOT_FOUND` | 404 | Không tìm thấy bản ghi thanh toán. |
| `PLAN_NOT_FOUND` | 404 | Người dùng chưa có kế hoạch trả nợ nào được lưu. |
| `DUPLICATE_DEBT` | 400 | Danh sách khoản nợ trong kế hoạch chứa mã nợ trùng lặp. |
| `STRATEGY_MISSING` | 400 | Thiếu thông tin chiến lược trả nợ. |
| `STRATEGY_DUPLICATE` | 400 | Hai chiến lược so sánh không được trùng nhau. |
| `EXTRA_PAYMENT_EXCEEDS_BUDGET`| 400 | Số tiền trả thêm vượt quá ngân sách khả dụng hàng tháng. |
| `SIMULATION_FAILED` | 500 | Quá trình mô phỏng thất bại hoặc vượt quá 600 tháng. |
| `NOTIFICATION_NOT_FOUND` | 404 | Không tìm thấy thông báo tương ứng. |
| `VALIDATION_FAILED` | 400 | Dữ liệu đầu vào không thỏa mãn các ràng buộc hợp lệ. |

---

## 8. Yêu cầu phi chức năng (Non-Functional Requirements)

- **NFR-SEC (Bảo mật):** Xác thực phi trạng thái (Stateless Authentication) qua JWT. Mật khẩu được băm an toàn với thuật toán BCrypt. Toàn bộ endpoint nhạy cảm yêu cầu kiểm tra quyền sở hữu đối tượng trước khi xử lý logic.
- **NFR-ACC (Độ chính xác số học):** Mọi phép toán tài chính, lãi suất, phân bổ tiền và số dư bắt buộc sử dụng kiểu dữ liệu `BigDecimal` với quy tắc làm tròn rõ ràng (`RoundingMode.HALF_UP`), tuyệt đối không dùng số thực `float`/`double` để tránh sai số dấu phẩy động.
- **NFR-PERF (Hiệu năng & Tối ưu truy vấn):**
  - Sử dụng `JOIN FETCH` khi nạp dữ liệu liên quan để triệt tiêu lỗi N+1 Query và `LazyInitializationException`.
  - Quá trình chạy mô phỏng 600 tháng được thực hiện hoàn toàn in-memory trên `DebtSnapshot`, thời gian đáp ứng endpoint `/compare` < 200ms.
  - Tác vụ định kỳ (Scheduler) xử lý theo từng lô (Batch Processing 100 bản ghi/trang).
- **NFR-REL (Độ tin cậy & Idempotency):** Áp dụng khóa duy nhất `referenceKey` cho các tác vụ tạo thông báo từ sự kiện để chống trùng lặp dữ liệu khi hệ thống thử lại (Retry/Redelivery).
- **NFR-MAIN (Khả năng bảo trì):** Phân chia kiến trúc rõ ràng theo từng Feature độc lập, tuân thủ nguyên lý SOLID, áp dụng Strategy Pattern cho phép dễ dàng mở rộng thêm các phương pháp tính lãi hoặc chiến lược trả nợ mới trong tương lai.
