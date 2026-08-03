# Swagger Test Guide — DebtWizard

Swagger UI: http://localhost:8080/swagger-ui/index.html

---

## CÁCH DÙNG SWAGGER VỚI JWT

1. Chạy **Login** → copy `accessToken` từ response
2. Nhấn nút **Authorize** (góc trên phải Swagger UI)
3. Nhập: `Bearer <accessToken>`
4. Nhấn **Authorize** → **Close**
5. Tất cả request sau đều tự đính kèm token

---

## FLOW THỰC HIỆN THEO THỨ TỰ

```
1. Register
2. Login  → lấy accessToken
3. Authorize trong Swagger
4. Update Profile (cập nhật income/expense để planning hoạt động)
5. Tạo 3 khoản nợ
6. Xem dashboard
7. Tạo payment cho debt 1
8. Xem phân tích (analysis)
9. Compare kế hoạch trả nợ
10. Lưu kế hoạch
11. Xem kế hoạch đã lưu
12. Xóa kế hoạch
13. Test các case lỗi
```

---

## 1. AUTH

### 1.1 Register ✅
**POST** `/api/auth/register`
```json
{
  "username": "testuser",
  "password": "password123",
  "fullName": "Nguyễn Văn Test",
  "email": "test@example.com",
  "monthlyIncome": 30000000
}
```
**Kết quả mong đợi:** 200, trả về `id`, `username`, `fullName`

---

### 1.2 Register — 400 Validation Error ❌
**POST** `/api/auth/register`
```json
{
  "username": "ab",
  "password": "123",
  "fullName": "",
  "email": "khong-phai-email",
  "monthlyIncome": -100
}
```
**Kết quả mong đợi:** 400, `result` chứa field errors

---

### 1.3 Register — 409 Username Exists ❌
**POST** `/api/auth/register`
```json
{
  "username": "testuser",
  "password": "password123",
  "fullName": "Người Khác",
  "email": "other@example.com",
  "monthlyIncome": 10000000
}
```
**Kết quả mong đợi:** 409, `code: USERNAME_ALREADY_EXISTS`

---

### 1.4 Login ✅
**POST** `/api/auth/login`
```json
{
  "username": "testuser",
  "password": "password123"
}
```
**Kết quả mong đợi:** 200, trả về `accessToken` và `refreshToken`
> ⚠️ Copy `accessToken` → Authorize trong Swagger

---

### 1.5 Login — 401 Sai mật khẩu ❌
**POST** `/api/auth/login`
```json
{
  "username": "testuser",
  "password": "satmatkha"
}
```
**Kết quả mong đợi:** 401, `code: INVALID_CREDENTIALS`

---

### 1.6 Refresh Token ✅
**POST** `/api/auth/refresh`
```json
{
  "refreshToken": "<refreshToken từ login>"
}
```
**Kết quả mong đợi:** 200, trả về `accessToken` mới

---

### 1.7 Refresh Token — 401 Token sai ❌
**POST** `/api/auth/refresh`
```json
{
  "refreshToken": "token.sai.hoàn.toàn"
}
```
**Kết quả mong đợi:** 401, `code: INVALID_TOKEN`

---

### 1.8 Logout ✅
**POST** `/api/auth/logout`
> Không cần body. Cần Authorization header.

**Kết quả mong đợi:** 200, `code: SUCCESS`

---

## 2. USER

### 2.1 Xem profile ✅
**GET** `/api/users/me`

**Kết quả mong đợi:** 200, trả về thông tin user

---

### 2.2 Cập nhật profile ✅
**PUT** `/api/users/me`
```json
{
  "fullName": "Nguyễn Văn Test Updated",
  "monthlyIncome": 30000000,
  "monthlyExpense": 8000000
}
```
**Kết quả mong đợi:** 200, `fullName` đã thay đổi
> ⚠️ **Quan trọng:** `monthlyIncome - monthlyExpense = 22,000,000`. Budget dùng cho planning = 22M - tổng minimum payment của các debt

---

### 2.3 Cập nhật — 400 Tên trống ❌
**PUT** `/api/users/me`
```json
{
  "fullName": "",
  "monthlyIncome": 20000000
}
```
**Kết quả mong đợi:** 400

---

### 2.4 Đổi mật khẩu ✅
**POST** `/api/users/change-password`
```json
{
  "oldPassword": "password123",
  "newPassword": "newpassword456",
  "confirmPassword": "newpassword456"
}
```
**Kết quả mong đợi:** 200
> ⚠️ Sau khi đổi xong, cần login lại với mật khẩu mới nếu muốn tiếp tục test

---

### 2.5 Đổi mật khẩu — 400 Quá ngắn ❌
**POST** `/api/users/change-password`
```json
{
  "oldPassword": "newpassword456",
  "newPassword": "123",
  "confirmPassword": "123"
}
```
**Kết quả mong đợi:** 400

---

## 3. DASHBOARD

### 3.1 Xem dashboard ✅
**GET** `/api/dashboard`

**Kết quả mong đợi:** 200, trả về tổng nợ, số khoản nợ, overdue, upcoming debts...

---

## 4. DEBT — TẠO 3 KHOẢN NỢ

### 4.1 Tạo Debt 1 — VPBank (REDUCING_BALANCE) ✅
**POST** `/api/debts`
```json
{
  "lenderName": "VPBank",
  "totalPrincipal": 50000000,
  "startDate": "2024-01-01",
  "termMonths": 24,
  "dueDay": 15,
  "debtType": "BANKING",
  "interestSettings": {
    "interestCalculationMethod": "REDUCING_BALANCE",
    "interestFrequency": "MONTHLY",
    "interestRate": 12.5
  }
}
```
**Kết quả mong đợi:** 200, trả về `id` (ghi nhớ là **debtId1**)

---

### 4.2 Tạo Debt 2 — Techcombank (FLAT) ✅
**POST** `/api/debts`
```json
{
  "lenderName": "Techcombank",
  "totalPrincipal": 20000000,
  "startDate": "2024-03-01",
  "termMonths": 12,
  "dueDay": 5,
  "debtType": "PERSONAL_LOAN",
  "interestSettings": {
    "interestCalculationMethod": "FLAT",
    "interestFrequency": "MONTHLY",
    "interestRate": 10.0
  }
}
```
**Kết quả mong đợi:** 200, trả về `id` (ghi nhớ là **debtId2**)

---

### 4.3 Tạo Debt 3 — Thẻ tín dụng (REDUCING_BALANCE, lãi cao) ✅
**POST** `/api/debts`
```json
{
  "lenderName": "Thẻ tín dụng MB Bank",
  "totalPrincipal": 15000000,
  "startDate": "2024-06-01",
  "termMonths": 18,
  "dueDay": 20,
  "debtType": "CREDIT",
  "interestSettings": {
    "interestCalculationMethod": "REDUCING_BALANCE",
    "interestFrequency": "MONTHLY",
    "interestRate": 22.0
  }
}
```
**Kết quả mong đợi:** 200, trả về `id` (ghi nhớ là **debtId3**)

---

### 4.4 Tạo Debt — 400 Validation Error ❌
**POST** `/api/debts`
```json
{
  "lenderName": "",
  "totalPrincipal": -1000,
  "startDate": null,
  "termMonths": 0,
  "dueDay": 32,
  "debtType": null,
  "interestSettings": {
    "interestCalculationMethod": null,
    "interestFrequency": null,
    "interestRate": 200
  }
}
```
**Kết quả mong đợi:** 400, field errors

---

### 4.5 Xem tất cả debt ✅
**GET** `/api/debts`

**Kết quả mong đợi:** 200, array 3 debt

---

### 4.6 Filter debt ACTIVE + sort theo remainingPrincipal ✅
**GET** `/api/debts?status=ACTIVE&sortBy=remainingPrincipal&sortDir=asc`

**Kết quả mong đợi:** 200, sorted tăng dần theo số dư

---

### 4.7 Filter theo interestMethod ✅
**GET** `/api/debts?interestMethod=REDUCING_BALANCE`

**Kết quả mong đợi:** 200, chỉ trả về VPBank và MB Bank

---

### 4.8 Search by lenderName ✅
**GET** `/api/debts?search=VP`

**Kết quả mong đợi:** 200, chỉ trả về VPBank

---

### 4.9 Xem debt theo ID ✅
**GET** `/api/debts/{debtId1}`

**Kết quả mong đợi:** 200, chi tiết VPBank

---

### 4.10 Xem debt — 404 ❌
**GET** `/api/debts/999999`

**Kết quả mong đợi:** 404, `code: DEBT_NOT_FOUND`

---

### 4.11 Update debt ✅
**PUT** `/api/debts/{debtId1}`
```json
{
  "lenderName": "VPBank - Vay mua xe"
}
```
**Kết quả mong đợi:** 200, `lenderName` đã đổi

---

### 4.12 Xem payments của debt ✅
**GET** `/api/debts/{debtId1}/payments`

**Kết quả mong đợi:** 200, array rỗng (chưa có payment)

---

## 5. PAYMENT

### 5.1 Tạo payment cho Debt 1 ✅
**POST** `/api/payments`
```json
{
  "debtId": <debtId1>,
  "amount": 2500000,
  "paymentMethod": "BANK_TRANSFER",
  "paymentDate": "2024-06-15",
  "note": "Trả tháng 6"
}
```
**Kết quả mong đợi:** 200, trả về `id`, `principalPaid`, `interestPaid` (interest-first)
> Ghi nhớ `id` là **paymentId1**

---

### 5.2 Tạo payment cho Debt 2 ✅
**POST** `/api/payments`
```json
{
  "debtId": <debtId2>,
  "amount": 1800000,
  "paymentMethod": "CASH",
  "paymentDate": "2024-06-05",
  "note": "Trả Techcombank tháng 6"
}
```
**Kết quả mong đợi:** 200

---

### 5.3 Tạo payment cho Debt 3 ✅
**POST** `/api/payments`
```json
{
  "debtId": <debtId3>,
  "amount": 1000000,
  "paymentMethod": "E_WALLET",
  "paymentDate": "2024-07-20",
  "note": "Trả thẻ MB"
}
```
**Kết quả mong đợi:** 200

---

### 5.4 Payment — 400 Số tiền âm ❌
**POST** `/api/payments`
```json
{
  "debtId": <debtId1>,
  "amount": -500,
  "paymentMethod": "CASH",
  "paymentDate": "2024-06-15"
}
```
**Kết quả mong đợi:** 400, `amount` validation error

---

### 5.5 Payment — 400 Ngày tương lai ❌
**POST** `/api/payments`
```json
{
  "debtId": <debtId1>,
  "amount": 1000000,
  "paymentMethod": "CASH",
  "paymentDate": "2099-01-01"
}
```
**Kết quả mong đợi:** 400

---

### 5.6 Payment — 400 Vượt quá số dư ❌
**POST** `/api/payments`
```json
{
  "debtId": <debtId1>,
  "amount": 999999999,
  "paymentMethod": "BANK_TRANSFER",
  "paymentDate": "2024-07-15"
}
```
**Kết quả mong đợi:** 400, `code: PAYMENT_EXCEEDS_REMAINING`

---

### 5.7 Xem tất cả payments ✅
**GET** `/api/payments`

**Kết quả mong đợi:** 200, array 3 payments với `lenderName`, `principalPaid`, `interestPaid`

---

### 5.8 Xem payment theo ID ✅
**GET** `/api/payments/{paymentId1}`

**Kết quả mong đợi:** 200, chi tiết payment

---

### 5.9 Xem payments của debt theo filter date ✅
**GET** `/api/debts/{debtId1}/payments?dateFrom=2024-01-01&dateTo=2024-12-31`

**Kết quả mong đợi:** 200, payments trong khoảng ngày

---

### 5.10 Payment — 404 Không tìm thấy ❌
**GET** `/api/payments/999999`

**Kết quả mong đợi:** 404, `code: PAYMENT_NOT_FOUND`

---

## 6. ANALYSIS

### 6.1 Xem tất cả phân tích ✅
**GET** `/api/analysis/all`

**Kết quả mong đợi:** 200, trả về:
- `dti`: Debt-to-Income ratio
- `interestRatio`: tỉ lệ lãi
- `overdue`: thông tin overdue
- `repaymentTime`: thời gian trả nợ ước tính
- `analysisDate`

---

## 7. PLANNING

> ⚠️ **Trước khi test planning, đảm bảo:**
> - User có `monthlyIncome: 30,000,000` và `monthlyExpense: 8,000,000`
> - Cả 3 debt đều ở trạng thái **ACTIVE** (không PAID_OFF)
> - `debtId1`, `debtId2`, `debtId3` còn active

### 7.1 Compare 2 chiến lược — 3 debt ✅
**POST** `/api/planning/compare`
```json
{
  "debtIds": [<debtId1>, <debtId2>, <debtId3>],
  "monthlyExtraPayment": 2000000,
  "firstStrategy": "MINIMIZE_INTEREST",
  "secondStrategy": "IMPROVE_CASHFLOW"
}
```
**Kết quả mong đợi:** 200, trả về:
- `maxAllowedExtraPayment`
- `firstPlan`: MINIMIZE_INTEREST — trả nợ lãi cao trước
- `secondPlan`: IMPROVE_CASHFLOW — trả nợ nhỏ trước giải phóng cashflow
- Mỗi plan có `totalInterestPaid`, `payoffDurationMonths`, `schedule`

---

### 7.2 Compare — extra = 0 ✅
**POST** `/api/planning/compare`
```json
{
  "debtIds": [<debtId1>, <debtId2>, <debtId3>],
  "monthlyExtraPayment": 0,
  "firstStrategy": "MINIMIZE_INTEREST",
  "secondStrategy": "IMPROVE_CASHFLOW"
}
```
**Kết quả mong đợi:** 200, không có extra payment, vẫn simulate đúng

---

### 7.3 Compare — 400 Cùng chiến lược ❌
**POST** `/api/planning/compare`
```json
{
  "debtIds": [<debtId1>, <debtId2>],
  "monthlyExtraPayment": 500000,
  "firstStrategy": "MINIMIZE_INTEREST",
  "secondStrategy": "MINIMIZE_INTEREST"
}
```
**Kết quả mong đợi:** 400, `code: STRATEGY_DUPLICATE`

---

### 7.4 Compare — 400 Extra vượt budget ❌
**POST** `/api/planning/compare`
```json
{
  "debtIds": [<debtId1>, <debtId2>, <debtId3>],
  "monthlyExtraPayment": 999999999,
  "firstStrategy": "MINIMIZE_INTEREST",
  "secondStrategy": "IMPROVE_CASHFLOW"
}
```
**Kết quả mong đợi:** 400, `code: EXTRA_PAYMENT_EXCEEDS_BUDGET`

---

### 7.5 Compare — 400 Danh sách trống ❌
**POST** `/api/planning/compare`
```json
{
  "debtIds": [],
  "monthlyExtraPayment": 500000,
  "firstStrategy": "MINIMIZE_INTEREST",
  "secondStrategy": "IMPROVE_CASHFLOW"
}
```
**Kết quả mong đợi:** 400, validation error

---

### 7.6 Compare — 400 debtIds trùng lặp ❌
**POST** `/api/planning/compare`
```json
{
  "debtIds": [<debtId1>, <debtId1>],
  "monthlyExtraPayment": 500000,
  "firstStrategy": "MINIMIZE_INTEREST",
  "secondStrategy": "IMPROVE_CASHFLOW"
}
```
**Kết quả mong đợi:** 400, `code: DUPLICATE_DEBT`

---

### 7.7 Lưu kế hoạch MINIMIZE_INTEREST ✅
**POST** `/api/planning/save`
```json
{
  "debtIds": [<debtId1>, <debtId2>, <debtId3>],
  "monthlyExtraPayment": 2000000,
  "strategy": "MINIMIZE_INTEREST"
}
```
**Kết quả mong đợi:** 200, trả về plan đầy đủ với:
- `id`, `strategy`, `planName`
- `totalInterestPaid`, `payoffDurationMonths`
- `schedule`: array các tháng, mỗi tháng có `payments` với detail từng khoản nợ

---

### 7.8 Lưu kế hoạch mới (replace cũ) ✅
**POST** `/api/planning/save`
```json
{
  "debtIds": [<debtId1>, <debtId2>, <debtId3>],
  "monthlyExtraPayment": 3000000,
  "strategy": "IMPROVE_CASHFLOW"
}
```
**Kết quả mong đợi:** 200, plan mới replace plan cũ

---

### 7.9 Xem kế hoạch đã lưu ✅
**GET** `/api/planning/saved`

**Kết quả mong đợi:** 200, trả về plan đầy đủ với schedule và debtPayments detail

---

### 7.10 Xem kế hoạch — 404 Chưa có plan ❌
> Cần chạy **Delete Plan** trước, sau đó mới test case này

**GET** `/api/planning/saved`

**Kết quả mong đợi:** 404, `code: PLAN_NOT_FOUND`

---

### 7.11 Xóa kế hoạch ✅
**DELETE** `/api/planning/saved`

**Kết quả mong đợi:** 200, `code: SUCCESS`

---

### 7.12 Xóa kế hoạch — 404 Không có gì để xóa ❌
**DELETE** `/api/planning/saved`
> Chạy sau bước 7.11

**Kết quả mong đợi:** 404, `code: PLAN_NOT_FOUND`

---

## 8. NOTIFICATION

### 8.1 Xem thông báo ✅
**GET** `/api/notifications`

**Kết quả mong đợi:** 200, array thông báo (có `PAYMENT_SUCCESS` từ các payment đã tạo)

---

### 8.2 Đánh dấu tất cả đã đọc ✅
**PATCH** `/api/notifications/read-all`

**Kết quả mong đợi:** 200

---

### 8.3 Đánh dấu 1 thông báo đã đọc ✅
**PATCH** `/api/notifications/{notificationId}/read`

**Kết quả mong đợi:** 200

---

### 8.4 Đánh dấu đọc — 404 ❌
**PATCH** `/api/notifications/999999/read`

**Kết quả mong đợi:** 404, `code: NOTIFICATION_NOT_FOUND`

---

## 9. TEST SECURITY

### 9.1 Gọi API không có token ❌
**GET** `/api/users/me`
> Không Authorize trong Swagger

**Kết quả mong đợi:** 401, `code: UNAUTHENTICATED`

---

### 9.2 Gọi API với token sai ❌
> Nhập token giả trong Authorize: `Bearer tokengiahoan.toan.sai`

**GET** `/api/users/me`

**Kết quả mong đợi:** 401

---

## 10. CLEANUP (sau khi test xong)

### 10.1 Xóa debt 3 (để test delete)
**DELETE** `/api/debts/{debtId3}`

**Kết quả mong đợi:** 200

### 10.2 Xóa debt — 404
**DELETE** `/api/debts/999999`

**Kết quả mong đợi:** 404

---

## BẢNG TÓM TẮT EXPECTED RESULTS

| # | API | Case | Expected |
|---|-----|------|----------|
| 1 | POST /auth/register | Happy path | 200 |
| 2 | POST /auth/register | Validation fail | 400 |
| 3 | POST /auth/register | Username exists | 409 |
| 4 | POST /auth/login | Happy path | 200 + tokens |
| 5 | POST /auth/login | Wrong password | 401 |
| 6 | POST /auth/refresh | Happy path | 200 |
| 7 | POST /auth/refresh | Invalid token | 401 |
| 8 | POST /auth/logout | Happy path | 200 |
| 9 | GET /users/me | Happy path | 200 |
| 10 | PUT /users/me | Update | 200 |
| 11 | POST /users/change-password | Happy path | 200 |
| 12 | GET /dashboard | Happy path | 200 |
| 13 | POST /debts | Debt 1 VPBank | 200 |
| 14 | POST /debts | Debt 2 Techcombank | 200 |
| 15 | POST /debts | Debt 3 MB Bank | 200 |
| 16 | POST /debts | Validation fail | 400 |
| 17 | GET /debts | List all | 200 |
| 18 | GET /debts?status=ACTIVE | Filter | 200 |
| 19 | GET /debts/{id} | By ID | 200 |
| 20 | GET /debts/999999 | Not found | 404 |
| 21 | PUT /debts/{id} | Update name | 200 |
| 22 | POST /payments | Payment debt 1 | 200 |
| 23 | POST /payments | Payment debt 2 | 200 |
| 24 | POST /payments | Payment debt 3 | 200 |
| 25 | POST /payments | Negative amount | 400 |
| 26 | POST /payments | Future date | 400 |
| 27 | POST /payments | Exceeds balance | 400 |
| 28 | GET /payments | All payments | 200 |
| 29 | GET /payments/{id} | By ID | 200 |
| 30 | GET /payments/999999 | Not found | 404 |
| 31 | GET /analysis/all | All analysis | 200 |
| 32 | POST /planning/compare | 3 debts, 2 strategies | 200 |
| 33 | POST /planning/compare | Extra = 0 | 200 |
| 34 | POST /planning/compare | Same strategy | 400 |
| 35 | POST /planning/compare | Exceeds budget | 400 |
| 36 | POST /planning/compare | Empty debtIds | 400 |
| 37 | POST /planning/compare | Duplicate debtIds | 400 |
| 38 | POST /planning/save | Save plan | 200 |
| 39 | GET /planning/saved | Get plan | 200 |
| 40 | DELETE /planning/saved | Delete plan | 200 |
| 41 | GET /planning/saved | After delete → 404 | 404 |
| 42 | GET /notifications | List | 200 |
| 43 | PATCH /notifications/read-all | Mark all read | 200 |

**Tổng: 43 cases — 32 Happy path ✅, 11 Error cases ❌**
