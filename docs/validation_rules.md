# Validation Rules — DebtWizard

## Áp dụng trong Simulation

**VR01 – Minimum Payment Cap**  
Nếu `minimumPayment > balance` tại một tháng → hệ thống tự động giới hạn xuống bằng `balance`, không trừ âm.

**VR02 – Extra Payment Cap**  
Nếu `monthlyExtraPayment > balance` của khoản nợ mục tiêu → chỉ trừ đúng `balance`, phần còn lại không được phân bổ tiếp.

**VR03 – Simulation Termination**  
Simulation kết thúc khi `balance ≤ 0` cho tất cả khoản nợ, hoặc khi đạt 600 tháng.

---

## Áp dụng khi đăng ký tài khoản

**VR04 – Username**  
Không được để trống, độ dài từ 3–50 ký tự.

**VR05 – Password (Register)**  
Không được để trống, tối thiểu 8 ký tự.

**VR06 – Full Name**  
Không được để trống, tối đa 100 ký tự.

**VR07 – Email**  
Không được để trống, phải đúng định dạng email.

**VR08 – Monthly Income**  
Không được để trống, phải lớn hơn hoặc bằng 0.

---

## Áp dụng khi đăng nhập

**VR09 – Login Username**  
Không được để trống, độ dài từ 3–50 ký tự.

**VR10 – Login Password**  
Không được để trống, tối thiểu 6 ký tự.

---

## Áp dụng khi tạo khoản nợ

**VR11 – Lender Name**  
Không được để trống, tối đa 100 ký tự.

**VR12 – Total Principal**  
`totalPrincipal` phải lớn hơn 0.

**VR13 – Start Date**  
Không được để trống.

**VR14 – Term Months**  
`termMonths` phải lớn hơn 0.

**VR15 – Due Day**  
`dueDay` phải nằm trong khoảng 1–31.

**VR16 – Debt Type**  
Không được để trống.

**VR17 – Interest Calculation Method**  
Không được để trống.

**VR18 – Interest Frequency**  
Không được để trống.

**VR19 – Interest Rate**  
Không được để trống, phải nằm trong khoảng 0–100 (%).

---

## Áp dụng khi ghi nhận thanh toán thực tế

**VR20 – Debt**  
`debtId` không được để trống.

**VR21 – Payment Amount**  
Phải lớn hơn 0.

**VR22 – Payment Method**  
Không được để trống.

**VR23 – Payment Date**  
Không được để trống, không được lớn hơn ngày hiện tại.

**VR24 – Payment Note**  
Nếu có thì tối đa 255 ký tự.

---

## Áp dụng khi lưu kế hoạch trả nợ

**VR25 – Debt Selection**  
Danh sách `debtIds` không được để trống.

**VR26 – Monthly Extra Payment**  
Không được để trống, phải lớn hơn hoặc bằng 0.

**VR27 – Repayment Strategy**  
Không được để trống.


**VR28 – Extra Payment Range** 
`monthlyExtraPayment` phải nằm trong khoảng `[0, monthlyIncome - expense - totalMinimumPayment]`.
Server tính `maxAllowedExtraPayment` và validate. Nếu vượt → trả lỗi `EXTRA_PAYMENT_EXCEEDS_MAX`.
Response `/compare` trả về `maxAllowedExtraPayment` để frontend hiển thị ngưỡng cho user.
Nếu `maxAllowedExtraPayment ≤ 0` → user không có khả năng trả thêm với thu chi hiện tại.