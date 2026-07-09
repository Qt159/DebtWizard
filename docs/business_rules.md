# Business Rules — DebtWizard

**BR01 – Minimum Payment**
Trong simulation, mỗi tháng hệ thống thanh toán minimum payment cho tất cả các khoản nợ còn active trước khi phân bổ extra payment.
Nếu `minimumPayment > balance` tại tháng đó → hệ thống tự động điều chỉnh xuống bằng `balance`.

**BR02 – Extra Payment**
Người dùng nhập thủ công số tiền `monthlyExtraPayment` khi gọi `/api/planning/compare` hoặc `/api/planning/save`.
Toàn bộ extra payment được phân bổ vào một khoản nợ duy nhất mỗi tháng — khoản nợ được chọn theo chiến lược.

Giá trị `monthlyExtraPayment` phải nằm trong khoảng:
- `extraPaymentMin = 0`
- `extraPaymentMax = monthlyIncome - expense - totalMinimumPayment` (của các debt được chọn)

Server **tính toán và validate** ngưỡng max này. Nếu user nhập vượt quá → trả lỗi `EXTRA_PAYMENT_EXCEEDS_MAX`.
Response của `/compare` trả về `maxAllowedExtraPayment` để frontend hiển thị ngưỡng cho user.

Nếu `maxAllowedExtraPayment ≤ 0` → user không có khả năng trả thêm, frontend nên cảnh báo.

> **Lưu ý:** Income và expense trong profile là con số tĩnh do user tự khai báo, không phản ánh biến động thu chi thực tế từng tháng. User có trách nhiệm điều chỉnh `monthlyExtraPayment` phù hợp với khả năng thực tế của mình.

**BR03 – Interest First (thực tế thanh toán)**
Khi ghi nhận thanh toán thực tế (`POST /api/payments`), tiền được phân bổ theo thứ tự interest-first:
- `interestPaid = min(amount, accruedInterest)`
- `principalPaid = min(amount - interestPaid, remainingPrincipal)`

**BR04 – Debt Closure**
Khoản nợ được tất toán khi `balance ≤ 0` trong simulation, hoặc khi `totalOutstanding ≤ 0` với thanh toán thực tế.
Khi tất toán: `paidOff = true`, minimum payment của khoản đó được giải phóng vào extra payment tháng tiếp theo.

**BR05 – Cashflow Release**
Khi một khoản nợ được tất toán trong simulation, `minimumPayment` của khoản đó được cộng tích lũy vào `monthlyExtraPayment` từ tháng tiếp theo trở đi.
Đây là cơ chế "snowball" — extra payment tăng dần theo thời gian khi các khoản nợ được tất toán lần lượt.

**BR06 – Interest Accrual (simulation)**
Mỗi tháng simulation, lãi được cộng vào balance trước khi trừ minimum payment:
`interest = balance × (interestRate / 100 / 12)`

**BR07 – Simulation Independence**
Hai simulation cho 2 chiến lược chạy hoàn toàn độc lập trên 2 bản sao riêng của DebtSnapshot.
Kết quả của simulation này không ảnh hưởng simulation kia.

**BR08 – Plan Selection**
Hệ thống không tự động chọn kế hoạch tốt nhất, chỉ cung cấp kết quả để người dùng quyết định.

**BR09 – Simulation Safety Limit**
Simulation tự động dừng sau 600 tháng (50 năm) để tránh vòng lặp vô hạn trong trường hợp dữ liệu bất thường.
