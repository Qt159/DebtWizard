# Validation Rules — DebtWizard

---

## Áp dụng trong Simulation

**VR01 – Minimum Payment Cap**
Nếu `minimumPayment > balance` tại một tháng → hệ thống tự động giới hạn xuống bằng `balance`, không trừ âm.

**VR02 – Extra Payment Cap**
Nếu `monthlyExtraPayment > balance` của khoản nợ mục tiêu → chỉ trừ đúng `balance`, phần còn lại không được phân bổ tiếp.

**VR03 – Simulation Termination**
Simulation kết thúc khi `balance ≤ 0` cho tất cả khoản nợ, hoặc khi đạt 600 tháng.

---

## Áp dụng khi tạo khoản nợ

**VR04 – Total Principal**
`totalPrincipal` phải lớn hơn 0.

**VR05 – Interest Rate**
`interestRate` phải lớn hơn hoặc bằng 0.

**VR06 – Term Months**
`termMonths` phải lớn hơn 0.

**VR07 – Due Day**
`dueDay` phải nằm trong khoảng 1–28.

---

## Áp dụng khi ghi nhận thanh toán thực tế

**VR08 – Debt Status**
Không thể ghi thanh toán cho khoản nợ đã `PAID_OFF` hoặc đã soft-delete.

**VR09 – Payment Date**
Ngày thanh toán không được trước `startDate` của khoản nợ.

---

## Dự kiến bổ sung

**VR10 – Monthly Income** *(chưa implement)*
Monthly income nên lớn hơn 0 để phân tích DTI có ý nghĩa.

**VR11 – Budget Sufficiency** *(chưa implement)*
Monthly income nên đủ để chi trả: `expense + totalMinimumPayment`.

**VR12 – Extra Payment Range** *(chưa implement)*
`monthlyExtraPayment` nên nằm trong khoảng `[0, monthlyIncome - expense - totalMinimumPayment]`.
Nếu user nhập vượt `extraPaymentMax` → hệ thống nên cảnh báo (simulation vẫn chạy nhưng kế hoạch không thực tế).
Nếu `extraPaymentMax ≤ 0` → hệ thống nên thông báo user không có khả năng trả thêm với thu chi hiện tại.
