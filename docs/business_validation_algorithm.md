# Business and Validation Rules - Algorithm

# Business Rules

**BR01 – Minimum Payment**
Người dùng phải thanh toán ít nhất mức minimum payment của tất cả các khoản nợ trong mỗi kỳ.
Nếu thu nhập không đủ để chi trả toàn bộ minimum payment → hệ thống đánh dấu kế hoạch là Not Feasible và cảnh báo người dùng.
**BR02 – Extra Payment Engine**
Hệ thống tính khoản extra payment từ thu nhập, chi phí sinh hoạt và tổng minimum payment.
Người dùng có thể override bằng cách nhập số tiền extra payment thủ công.
**BR03 – Budget Validation**
Thu nhập hàng tháng phải đủ để chi trả chi phí sinh hoạt và tổng minimum payment.
Nếu không đủ → hệ thống từ chối tạo kế hoạch trả nợ.

**BR04 – Debt Closure**
Khoản nợ được xem là tất toán khi số dư còn lại nhỏ hơn hoặc bằng 0.
Nếu thanh toán vượt dư nợ (negative balance) → phần dư được cộng vào extra payment tháng kế tiếp.

**BR05 – Payment Reallocation**
Khi một khoản nợ được tất toán, minimum payment của khoản đó được giải phóng và cộng vào extra payment.
Extra payment mới sẽ được phân bổ theo chiến lược hiện tại (SaveMoney hoặc CashFlow).

**BR06 – Interest Calculation**
Tiền lãi được tính dựa trên dư nợ còn lại và lãi suất của khoản nợ.

**BR07 – Plan Generation**
Hệ thống sinh tất cả các kế hoạch trả nợ từ cùng một bộ dữ liệu đầu vào.

**BR08 – Plan Selection**
Hệ thống không tự động chọn kế hoạch tốt nhất, chỉ cung cấp kết quả để người dùng quyết định.

**BR09 – Simulation Completion**
Mô phỏng kết thúc khi tất cả các khoản nợ được tất toán.

# Validation Rules

**VR01 – Debt Balance**
Debt balance phải lớn hơn 0.

**VR02 – Interest Rate**
Interest rate phải lớn hơn hoặc bằng 0.

**VR03 – Minimum Payment**
Trong quá trình trả nợ, nếu minimum payment > remaining balance → hệ thống tự động điều chỉnh bằng remaining balance.

**VR04 – Monthly Income**
Monthly income phải lớn hơn 0.

**VR05 – Monthly Expenses**
Monthly expenses phải lớn hơn hoặc bằng 0.

**VR06 – Budget Sufficiency**
Monthly income phải lớn hơn hoặc bằng: Monthly expenses + Total minimum payments.

**VR07 – Debt Requirement**
Người dùng phải có ít nhất một khoản nợ để tạo kế hoạch trả nợ.

**VR08 – Debt Consistency**
Minimum payment không được lớn hơn debt balance tại thời điểm tạo khoản nợ.

# Algorithm
**Save Money Strategy**
Rule: Chọn khoản nợ có Interest Rate cao nhất

**Cash Flow Strategy**
Rule:
- Tính Priority Score = MonthlyPayment / EstimatedPayoffMonths
- Chọn debt có Priority Score cao nhất

- **Simulation**

Input: Debt List, Monthly Income, Monthly Expenses, RepaymentStrategy
Output: Repayment Schedule, Total Interest Paid, Payoff Duration (Months)
Steps:
1. Tính tổng minimum payment của tất cả các khoản nợ.
2. Tính khoản Available Extra Payment = Monthly Income - Monthly Expenses - Total Minimum Payment.
3. Trong khi vẫn còn khoản nợ chưa tất toán (Debt Balance > 0):
    a. Thanh toán minimum payment cho tất cả các khoản nợ còn active.
    b. Xác định khoản nợ ưu tiên dựa trên RepaymentStrategy.
    c. Phân bổ toàn bộ Available Extra Payment cho khoản nợ ưu tiên.
    d. Cập nhật dư nợ còn lại cho từng khoản nợ.
    e. Tính lãi phát sinh dựa trên dư nợ còn lại và lãi suất.
    f. Nếu một khoản nợ được tất toán:
        i. Cập nhật trạng thái khoản nợ là PAID_OFF.
        ii. Giải phóng minimum Payment của khoản nợ đã tất toán.
        iii. Cộng phần Minimum Payment được giải phóng vào Available Extra Payment.
    g. Lưu kết quả vào Repayment Schedule.
4. Lặp lại cho đến khi tất cả các khoản nợ đều được tất toán.
5. Tính tổng lãi đã trả và số tháng tất toán nợ (Payoff Duration).
6. Trả về Repayment Schedule, Total Interest Paid, Payoff Duration (Months).


**Quick Win Detection Algorithm**
- Input: Repayment Schedule từ Simulation.
- Output: QuickWinResponse (List + Best Opportunity)
- Steps:
1. Duyệt từng tháng trong Repayment Schedule.
2. Với mỗi debt còn active:
   a. Tính Additional Payment Required để tất toán khoản nợ trong tháng đó.
   b. Tính Monthly Payment Released nếu khoản nợ được tất toán.
   c. Tính số tháng payoff được rút ngắn (Expected Duration Reduction).
3. Tính Quick Win Score = Monthly Payment Released * Expected Duration Reduction / Additional Payment Required.(Ưu tiên các khoản nợ có Quick Win Score cao hơn)
4. Tạo danh sách Quick Win Opportunities với các thông tin: Month, Debt Name, Additional Payment Required, Monthly Payment Released, Expected Duration Reduction, Quick Win Score.
5. Sort danh sách theo Quick Win Score giảm dần.
6. Chọn Best Opportunity là khoản nợ có Quick Win Score cao nhất.
7. QuickWinResponse:
   - opportunities (List<QuickWinOpportunity>)
   - bestOpportunity (QuickWinOpportunity)
