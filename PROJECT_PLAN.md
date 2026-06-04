# Kế Hoạch Dự Án: WealthWise - Ứng Dụng Quản Lý Thu Chi Thông Minh

## 1. Tổng Quan Dự Án
**WealthWise** là một ứng dụng web giúp người dùng quản lý tài chính cá nhân hiệu quả thông qua việc ghi chép thu chi, thiết lập ngân sách, theo dõi qua biểu đồ và nhận các gợi ý tối ưu hóa tài chính từ Trí Tuệ Nhân Tạo (AI).

**Công nghệ sử dụng:**
- **Frontend:** Angular, HTML/CSS/TypeScript.
- **Backend:** Spring Boot (Java), Spring Data JPA, Spring Security (JWT).
- **Cơ sở dữ liệu:** MySQL (hoặc SQL Server).
- **Tích hợp:** Google OAuth2 (Đăng nhập Google), API AI (OpenAI/Gemini/Claude) cho tính năng gợi ý.

---

## 2. Gợi ý Thiết kế Cơ sở dữ liệu (ERD - Entity Relationship Diagram)

Dưới đây là cấu trúc các bảng chính cho dự án.

### Bảng `users`
Lưu trữ thông tin người dùng.
- `id` (UUID, Primary Key): Mã định danh duy nhất.
- `username` (VARCHAR, Unique): Tên đăng nhập (hoặc email).
- `password` (VARCHAR): Mật khẩu đã được mã hóa (băm).
- `email` (VARCHAR, Unique): Email người dùng.
- `first_name` (VARCHAR): Tên.
- `last_name` (VARCHAR): Họ.
- `avatar_url` (VARCHAR, Nullable): Đường dẫn ảnh đại diện.
- `auth_provider` (VARCHAR): Nhà cung cấp xác thực ('local', 'google').
- `created_at` (TIMESTAMP): Thời gian tạo tài khoản.
- `updated_at` (TIMESTAMP): Thời gian cập nhật lần cuối.

### Bảng `categories`
Lưu trữ các danh mục thu/chi do người dùng tạo.
- `id` (UUID, Primary Key): Mã định danh.
- `user_id` (UUID, Foreign Key -> `users.id`): Liên kết tới người dùng sở hữu danh mục.
- `name` (VARCHAR): Tên danh mục (VD: "Ăn uống", "Lương tháng 5").
- `type` (ENUM('income', 'expense')): Loại danh mục (Thu hoặc Chi).
- `icon` (VARCHAR, Nullable): Tên icon (VD: 'fastfood', 'work').
- `color` (VARCHAR, Nullable): Mã màu hex (VD: '#FF5733').
- `created_at` (TIMESTAMP).

### Bảng `transactions`
Bảng quan trọng nhất, lưu trữ mọi giao dịch thu/chi.
- `id` (UUID, Primary Key): Mã định danh.
- `user_id` (UUID, Foreign Key -> `users.id`): Giao dịch này của ai.
- `category_id` (UUID, Foreign Key -> `categories.id`): Giao dịch này thuộc danh mục nào.
- `amount` (DECIMAL): Số tiền giao dịch.
- `type` (ENUM('income', 'expense')): Loại giao dịch (phải khớp với `categories.type`).
- `description` (TEXT, Nullable): Ghi chú chi tiết cho giao dịch.
- `transaction_date` (DATE): Ngày diễn ra giao dịch.
- `created_at` (TIMESTAMP).
- `updated_at` (TIMESTAMP).

### Bảng `budgets`
Lưu trữ ngân sách người dùng đặt ra cho mỗi danh mục.
- `id` (UUID, Primary Key): Mã định danh.
- `user_id` (UUID, Foreign Key -> `users.id`): Ngân sách của ai.
- `category_id` (UUID, Foreign Key -> `categories.id`): Ngân sách cho danh mục nào.
- `amount` (DECIMAL): Số tiền ngân sách tối đa.
- `month` (INT): Tháng áp dụng (1-12).
- `year` (INT): Năm áp dụng.
- `created_at` (TIMESTAMP).

---

## 3. Danh Sách Tính Năng (Tính năng cốt lõi & Nâng cao)

### Tính năng cốt lõi (Core Features)
1. **Xác thực người dùng (Authentication & Authorization):**
   - Đăng nhập / Đăng ký truyền thống (Username/Password).
   - Đăng nhập bằng tài khoản mạng xã hội (Google Login).
   - Phân quyền bảo mật bằng JWT.
   - Quản lý tài khoản (Đổi mật khẩu, cập nhật thông tin cá nhân).
2. **Quản lý Thu / Chi (Transactions):**
   - Thêm, sửa, xóa, xem danh sách giao dịch (thu/chi).
   - Phân loại giao dịch (Ăn uống, Mua sắm, Lương, v.v.).
   - Lọc và tìm kiếm giao dịch theo thời gian, danh mục, số tiền.
3. **Quản lý Danh mục (Categories):**
   - Tạo các danh mục thu/chi tùy chỉnh.
   - Gán icon/màu sắc cho từng danh mục.
4. **Quản lý Ngân sách (Budgets):**
   - Thiết lập ngân sách chi tiêu hàng tháng cho từng danh mục.
   - Cảnh báo khi chi tiêu sắp vượt ngân sách (Progress bar đỏ).
5. **Báo cáo & Thống kê (Reports & Analytics):**
   - Biểu đồ tròn (Pie chart) cơ cấu chi tiêu.
   - Biểu đồ cột/đường (Bar/Line chart) xu hướng thu/chi theo tháng/năm.

### Tính năng nâng cao (Advanced Features)
6. **Gợi ý Tài chính bằng AI (AI Financial Advisor):**
   - **Phân tích hành vi:** Phân tích dữ liệu chi tiêu trong tháng và đưa ra nhận xét (VD: "Tháng này bạn chi quá nhiều cho ăn uống ngoài...").
   - **Gợi ý tiết kiệm:** Đề xuất cách cắt giảm chi tiêu hợp lý để đạt mục tiêu ngân sách.
   - **Dự báo:** Dự báo số tiền còn dư cuối tháng dựa trên tốc độ chi tiêu hiện tại.
   - Chabot trợ lý tài chính cơ bản.
7. **Thông báo (Notifications):**
   - Nhắc nhở nhập giao dịch hàng ngày.
   - Cảnh báo vượt ngân sách.

---

## 4. Phân Công Công Việc (Dành cho nhóm 4 người)

Để đảm bảo dự án chạy mượt mà, công việc được chia theo mô hình **Cross-functional (Kết hợp Frontend và Backend)** dựa trên từng Module tính năng, kết hợp chuyên môn hóa.

### 👤 Thành viên 1: Team Leader & Core Architecture (Lead Backend & Security)
**Nhiệm vụ:**
- Xây dựng cấu trúc dự án Spring Boot và Angular từ ban đầu.
- Cấu hình Database, thiết kế sơ đồ CSDL (ERD).
- Phát triển toàn bộ Module **Xác thực (Auth)**: JWT, Đăng nhập, Đăng ký, Tích hợp Google OAuth2.
- Xử lý các vấn đề chung về cấu hình ứng dụng, xử lý lỗi (Exception Handling) ở Backend.
- Ghép nối Layout chung cho Frontend (Sidebar, Header, Routing cơ bản).

### 👤 Thành viên 2: Fullstack Developer - Module Quản lý Cốt lõi
**Nhiệm vụ:**
- Phát triển API Backend cho **Giao dịch (Transactions)** và **Danh mục (Categories)** (CRUD operations).
- Phát triển giao diện (UI) và logic (Frontend) cho trang Giao dịch và Danh mục.
- Xử lý tính năng Lọc, Tìm kiếm, Phân trang giao dịch.
- Đảm bảo tính toàn vẹn dữ liệu khi người dùng thêm/xóa giao dịch.

### 👤 Thành viên 3: Fullstack Developer - Module Ngân sách & Thống kê
**Nhiệm vụ:**
- Phát triển API Backend tính toán dữ liệu **Ngân sách (Budgets)** và tổng hợp dữ liệu cho **Báo cáo (Reports)**.
- Phát triển giao diện trang Ngân sách (Thanh tiến trình, cảnh báo).
- Tích hợp thư viện biểu đồ (ví dụ: Chart.js, ng2-charts, ECharts) vào Frontend trang Thống kê/Dashboard.
- Xử lý UI/UX trang Dashboard hiển thị thông tin tổng quan (Tổng thu, Tổng chi, Số dư).

### 👤 Thành viên 4: Chuyên viên AI Integration & UI/UX Polish
**Nhiệm vụ:**
- Nghiên cứu và tích hợp API của AI (như OpenAI API, Gemini API, hoặc API tạo sinh nội dung khác) vào Backend Spring Boot.
- Xây dựng luồng gửi dữ liệu tổng hợp (dạng JSON/văn bản nén) lên AI và nhận kết quả phản hồi.
- Xây dựng giao diện tính năng **Gợi ý Tài chính (AI Advisor)**: Khung chat, popup gợi ý, hoặc widget báo cáo AI.
- Quản lý chức năng Cài đặt (Settings), Quản lý người dùng.
- Chuốt lại UI/UX (CSS, Animations, Responsive) cho toàn bộ ứng dụng trước khi nộp.

---

## 5. Kế Hoạch Triển Khai (Dự kiến 8 - 10 tuần)

### Giai đoạn 1: Khởi tạo & Thiết kế (Tuần 1 - 2)
- Thống nhất Requirement, vẽ sơ đồ Database (ERD), thiết kế Figma/Wireframe UI.
- Khởi tạo project Spring Boot và Angular. Đẩy code lên GitHub.
- **TV1** làm chức năng Auth cơ bản.

### Giai đoạn 2: Phát triển Tính năng Cốt lõi (Tuần 3 - 5)
- **TV1:** Hoàn thiện Google Login, Middleware phân quyền.
- **TV2:** Hoàn thiện CRUD Transactions & Categories từ API đến UI.
- **TV3:** Xây dựng khung giao diện Dashboard, làm tính năng thiết lập Ngân sách.
- **TV4:** Chuẩn bị môi trường gọi API AI, xây dựng layout trang Settings.

### Giai đoạn 3: Phân tích, Biểu đồ & Tích hợp AI (Tuần 6 - 7)
- **TV2 & TV3:** Gắn dữ liệu thật vào biểu đồ trên Dashboard và trang Reports. Hoàn thiện tính toán Ngân sách so với thực tế chi tiêu.
- **TV4:** Gắn dữ liệu thống kê từ TV3 đẩy sang API AI để lấy text gợi ý, hiển thị lên UI cho người dùng.

### Giai đoạn 4: Kiểm thử, Tối ưu & Báo cáo (Tuần 8)
- Test chéo (Cross-testing) các chức năng: Đảm bảo luồng tạo giao dịch -> Trừ ngân sách -> Biểu đồ cập nhật -> AI phân tích chạy mượt mà.
- **TV4:** Tối ưu hóa UI (Responsive cho mobile).
- Viết tài liệu báo cáo, chuẩn bị slide bảo vệ dự án.

---
## 6. Quy trình làm việc nhóm (Workflow)
- **Quản lý Source Code:** Sử dụng GitHub. Code phải được phân nhánh (Branching): `main`, `dev`, `feature/tên-tính-năng`. Sử dụng Pull Request (PR) để review code trước khi gộp (merge).
- **Quản lý Task:** Dùng Trello hoặc Notion/Jira để tạo bảng Kanban (To Do - In Progress - Review - Done).
- **Họp nhóm:** Sync-up 15-30 phút mỗi 2-3 ngày để báo cáo tiến độ và giải quyết vướng mắc.
