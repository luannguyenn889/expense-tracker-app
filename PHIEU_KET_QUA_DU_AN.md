# PHIẾU TỔNG HỢP KẾT QUẢ DỰ ÁN NHÓM
**Học phần: Công nghệ Web**

## 1. Thông tin chung
* **Tên đề tài:** WealthWise - Ứng Dụng Quản Lý Thu Chi Thông Minh
* **Tên nhóm:** [Tên nhóm của bạn]
* **Số thành viên:** 2
* **Công nghệ Frontend:** Angular, TypeScript, CSS, Chart.js
* **Công nghệ Backend:** Java Spring Boot, Spring Security (JWT), Spring Data JPA
* **Hệ quản trị cơ sở dữ liệu:** MySQL

---

## 2. Kết quả thực hiện dự án (Tiến độ hiện tại)

### 2.1 Frontend
| STT | Nội dung chức năng | Thành viên thực hiện |
| :---: | --- | :---: |
| 1 | **Xác thực người dùng:** Giao diện Đăng nhập, Đăng ký thông thường và tích hợp nút Đăng nhập nhanh bằng tài khoản Google (Google Login API). | Nguyễn Thành Luân |
| 2 | **Quản lý Danh mục (Categories):** Giao diện CRUD (Thêm, Sửa, Xóa, Xem danh sách) danh mục thu/chi. Hỗ trợ tùy chỉnh tên danh mục, icon hiển thị, màu sắc đại diện, mô tả chi tiết và thiết lập hạn mức chi tiêu hàng tháng (`monthlyBudget`). | Nguyễn Thành Luân |
| 3 | **Dashboard & Thống kê:** Hiển thị danh mục chi tiêu gần đây và vẽ biểu đồ tròn (Pie/Doughnut Chart) thống kê mức chi tiêu của các danh mục loại `EXPENSE` (sử dụng thư viện Chart.js). | Nguyễn Thành Luân |
| 4 | **Quản lý Ví (Wallet):** Giao diện CRUD ví tiền cá nhân, hiển thị danh sách ví hiện có và tổng số dư khả dụng của tất cả các ví. | Thùy Trang |
| 5 | **Tư vấn tài chính AI:** Khung hiển thị lời khuyên tài chính cá nhân hóa từ AI. | Nguyễn Thành Luân |
| 6 | **Cập nhật thông tin cá nhân:** Form cập nhật thông tin người dùng (User Profile). | Nguyễn Thành Luân |

### 2.2 Backend
| STT | Nội dung chức năng | Thành viên thực hiện |
| :---: | --- | :---: |
| 1 | **API Xác thực (Auth API):** Xử lý đăng nhập, đăng ký, cấp phát JWT, cập nhật thông tin User profile và xác thực token Google OAuth2 (`/api/auth/google-login`). | Nguyễn Thành Luân |
| 2 | **API Quản lý Danh mục (Category API):** Xử lý nghiệp vụ CRUD danh mục và lọc các danh mục tương ứng với người dùng đang đăng nhập. | Nguyễn Thành Luân |
| 3 | **API Quản lý Ví (Wallet API):** Xử lý nghiệp vụ CRUD ví (tạo mới, cập nhật, xóa ví), lấy danh sách ví của user và tính toán tổng số dư các ví (`/api/wallets/total-balance`). | Thùy Trang |
| 4 | **API Tư vấn tài chính AI (AI API):** Tích hợp dịch vụ gọi API của Google Gemini để phân tích hành vi và tư vấn tài chính dựa trên dữ liệu người dùng (`/api/ai/advice`). | Nguyễn Thành Luân |
| 5 | **Cấu hình Core & DB:** Thiết kế Database, cấu hình CORS, kết nối cơ sở dữ liệu MySQL và quản lý Exception tập trung. | Nguyễn Thành Luân |

---

## 3. Tích hợp dịch vụ bên thứ 3 (Điểm cộng)
Các dịch vụ đã tích hợp thành công:
* [x] Google Login
* [x] Chatbot AI (Google Gemini API)
* [ ] Email Service
* [ ] Facebook Login
* [ ] Firebase
* [ ] Cloudinary
* [ ] Google Maps API
* [ ] OpenAI API
* [ ] VNPay
* [ ] MoMo
* [ ] Khác: ............................................................

**Mô tả ngắn gọn về các dịch vụ đã tích hợp:**
* **Google Login:** Tích hợp Google OAuth2 cho phép người dùng đăng nhập trực tiếp bằng tài khoản Google. Client gửi `idToken` lên backend để xác thực thông tin và tạo tài khoản/phiên đăng nhập an toàn mà không cần nhập mật khẩu.
* **Chatbot AI (Google Gemini API):** Tích hợp model `gemini-flash-lite-latest` thông qua Google Gemini API để tự động phân tích và tạo ra các đề xuất, lời khuyên tài chính thông minh, giúp người dùng tối ưu hóa chi tiêu cá nhân dựa trên dữ liệu thật.

---

## 4. Tổng hợp đóng góp của các thành viên
| MSSV | Họ và tên | Công việc chính phụ trách | Tỷ lệ đóng góp (%) |
| --- | --- | --- | :---: |
| [MSSV của Luân] | Nguyễn Thành Luân | Thiết lập dự án, làm module Xác thực (Đăng nhập/Đăng ký/Google Login), Quản lý Danh mục (Categories), Dashboard & vẽ biểu đồ Chart.js, tích hợp Gemini AI. | 50% |
| [MSSV của Trang] | Thùy Trang | Phát triển module Quản lý Ví (Wallet Management) cả Frontend & Backend bao gồm thêm, sửa, xóa ví và hiển thị tổng số dư tài khoản. | 50% |

**Tổng tỷ lệ đóng góp: 100%**

---

## 5. Ma trận đóng góp theo công việc
*(Đánh dấu ✓ vào các nội dung thành viên trực tiếp thực hiện)*

| STT | Chức năng / Module công việc | Nguyễn Thành Luân | Thùy Trang |
| :---: | --- | :---: | :---: |
| 1 | Thiết kế sơ đồ thực thể liên kết (ERD) và cấu trúc DB | ✓ | ✓ |
| 2 | Chức năng Đăng nhập & Đăng ký (Auth) | ✓ | |
| 3 | Tích hợp Đăng nhập bằng Google Login | ✓ | |
| 4 | CRUD Danh mục (Category) | ✓ | |
| 5 | Dashboard hiển thị danh mục gần đây | ✓ | |
| 6 | Thống kê biểu đồ chi tiêu (Doughnut Chart) bằng Chart.js | ✓ | |
| 7 | Tích hợp dịch vụ tư vấn tài chính bằng AI (Gemini API) | ✓ | |
| 8 | CRUD Ví (Wallet) | | ✓ |
| 9 | Tính toán tổng số dư tất cả các ví | | ✓ |
| 10 | Cập nhật thông tin cá nhân (User Profile) | ✓ | |

---

## 6. Tự đánh giá của nhóm
* **Mức độ hoàn thành đề tài:** 75% (đã hoàn thiện các module nền tảng quan trọng nhất: Auth, Google OAuth2, Wallet, Category, Dashboard Chart và AI Advice).
* **Chức năng nổi bật nhất:**
  * Tính năng **tư vấn tài chính thông minh bằng AI** (Gemini API) dựa trên dữ liệu người dùng.
  * **Biểu đồ tròn trực quan** (Chart.js) tự động lọc và vẽ phân bổ ngân sách chi tiêu thực tế từ các danh mục.
  * **Đăng nhập bằng tài khoản Google** giúp nâng cao trải nghiệm người dùng.
* **Chức năng chưa hoàn thành (nếu có):** Chức năng CRUD Giao dịch (Transactions), thiết lập hạn mức ngân sách chi tiết (Budgets) và hệ thống lịch sử báo cáo nâng cao (Reports).
* **Khó khăn lớn nhất:**
  * Đồng bộ luồng xử lý bất đồng bộ của Angular để vẽ biểu đồ canvas động của Chart.js khi dữ liệu danh mục được fetch từ backend về trễ.
  * Phân quyền JWT an toàn và quản lý cấu hình bảo mật CORS khi giao tiếp giữa Angular và Spring Boot.
* **Link GitHub:** [https://github.com/luannguyenn889/expense-tracker-app](https://github.com/luannguyenn889/expense-tracker-app)

---

## 7. Cam kết
Chúng tôi cam kết các thông tin trên phản ánh đúng quá trình thực hiện dự án và mức độ tham gia của từng thành viên.

| MSSV | Họ và tên | Ký tên |
| --- | --- | --- |
| [MSSV của Luân] | Nguyễn Thành Luân | |
| [MSSV của Trang] | Thùy Trang | |
