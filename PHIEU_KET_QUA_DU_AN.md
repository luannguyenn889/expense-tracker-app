# PHIẾU TỔNG HỢP KẾT QUẢ DỰ ÁN NHÓM
**Học phần: Công nghệ Web**

## 1. Thông tin chung
* **Tên đề tài:** Xây dựng ứng dụng web quản lý chi tiêu cá nhân (WealthWise)
* **Tên nhóm:** Nhóm 4
* **Số thành viên:** 4
* **Công nghệ Frontend:** Angular
* **Công nghệ Backend:** Spring Boot
* **Hệ quản trị cơ sở dữ liệu:** MySQL

---

## 2. Kết quả thực hiện dự án

### 2.1 Frontend
| STT | Nội dung chức năng | Thành viên thực hiện |
| :---: | --- | :---: |
| 1 | **Xác thực người dùng:** Giao diện Đăng nhập, Đăng ký thông thường và tích hợp nút Đăng nhập nhanh bằng tài khoản Google (Google Login API). | Nguyễn Hồ Thế Luân |
| 2 | **Quản lý Danh mục:** Giao diện CRUD danh mục thu/chi. Hỗ trợ tùy chỉnh tên, icon, màu sắc đại diện, mô tả chi tiết và thiết lập hạn mức chi tiêu hàng tháng. | Nguyễn Hồ Thế Luân |
| 3 | **Cập nhật thông tin cá nhân:** Form cập nhật thông tin người dùng được tích hợp trực tiếp vào trang Cài đặt. | Nguyễn Hồ Thế Luân |
| 4 | **Tư vấn tài chính AI:** Khung hiển thị lời khuyên tài chính cá nhân hóa từ AI dựa trên phân tích dữ liệu chi tiêu thật. | Nguyễn Hồ Thế Luân |
| 5 | **Trợ lý ảo AI Chatbot:** Giao diện ô chat nổi tiện ích ở góc màn hình, hỗ trợ giao tiếp bằng tiếng Việt, hỏi đáp tình hình tài chính và nhập nhanh giao dịch từ ngôn ngữ tự nhiên. | Nguyễn Hồ Thế Luân |
| 6 | **Quản lý Ví:** Giao diện CRUD ví tiền cá nhân, hiển thị danh sách ví hiện có và tổng số dư khả dụng của tất cả các ví. | Trương Thị Thùy Trang |
| 7 | **Quản lý Giao dịch:** Giao diện CRUD giao dịch (Thu nhập, Chi tiêu) phân trang, bộ lọc lịch sử giao dịch và giao diện chuyển khoản nội bộ liên ví. | Trương Thị Thùy Trang |
| 8 | **Quản lý Hạn mức ngân sách:** Giao diện thiết lập và hiển thị tiến trình sử dụng ngân sách từng danh mục, tự động cảnh báo khi chi tiêu đạt mức nguy hiểm (80%) hoặc vượt hạn mức (100%). | Trần Minh Vương |
| 9 | **Quản lý Mục tiêu tiết kiệm:** Giao diện quản lý các quỹ tích lũy mục tiêu (tên, số tiền, ngày hạn). Hỗ trợ nạp tiền trực tiếp từ các ví cá nhân, theo dõi thanh tiến độ (%) và hiển thị trạng thái (Đang thực hiện, Đã hoàn thành, Quá hạn). Khi xóa hỗ trợ tùy chọn hoàn tiền tiết kiệm về lại ví. | Trần Minh Vương |
| 10 | **Báo cáo & Phân tích:** Biểu đồ cột phân tích luồng tiền Thu/Chi theo tháng, biểu đồ tròn (SVG) phân bổ ngân sách, bộ lọc thời gian và xuất báo cáo động. | Ngô Quốc Trung |
| 11 | **Dashboard & Thống kê:** Hiển thị danh mục chi tiêu gần đây, số dư ví hiện tại, dòng tiền ròng và biểu đồ thống kê mức chi tiêu. | Ngô Quốc Trung |

### 2.2 Backend
| STT | Nội dung chức năng | Thành viên thực hiện |
| :---: | --- | :---: |
| 1 | **API Xác thực:** Xử lý đăng nhập, đăng ký, cấp phát JWT, cập nhật thông tin User profile và xác thực token Google OAuth2 (/api/auth/google-login). | Nguyễn Hồ Thế Luân |
| 2 | **API Tư vấn tài chính AI:** Tích hợp dịch vụ gọi API của Google Gemini để phân tích hành vi và tư vấn tài chính dựa trên dữ liệu người dùng (/api/ai/advice). | Nguyễn Hồ Thế Luân |
| 3 | **API Quản lý Danh mục:** Xử lý nghiệp vụ CRUD danh mục và lọc các danh mục tương ứng với người dùng đang đăng nhập. | Nguyễn Hồ Thế Luân |
| 4 | **API Trợ lý ảo Chatbot AI:** Endpoint nhận câu lệnh và ngữ cảnh tài chính của user (/api/transactions/chat), gọi Google Gemini API để phân tích tự động, trò chuyện tư vấn hoặc trích xuất thông tin giao dịch để ghi chép tự động. | Nguyễn Hồ Thế Luân |
| 5 | **API Quản lý Ví:** Xử lý nghiệp vụ CRUD ví (tạo mới, cập nhật, xóa ví), lấy danh sách ví của user và tính toán tổng số dư các ví. | Trương Thị Thùy Trang |
| 6 | **API Giao dịch & Chuyển tiền:** CRUD giao dịch phân trang, bộ lọc nâng cao, và xử lý chuyển tiền giữa 2 ví cùng chủ sở hữu. | Trương Thị Thùy Trang |
| 7 | **API Xuất báo cáo:** Xuất báo cáo Excel (XLSX) và PDF có lọc theo khoảng thời gian tùy chọn của người dùng. | Ngô Quốc Trung |
| 8 | **Cấu hình Core và DB:** Thiết kế Database, cấu hình CORS, kết nối cơ sở dữ liệu MySQL và quản lý Exception tập trung. | Nguyễn Hồ Thế Luân |
| 9 | **API Quản lý Hạn mức ngân sách:** Xử lý CRUD hạn mức chi tiêu cho từng danh mục theo tháng/năm, tính toán tiến trình sử dụng ngân sách thực tế và trả về tỷ lệ phần trăm đã tiêu dùng để hiển thị/cảnh báo trên giao diện. | Trần Minh Vương |
| 10 | **API Mục tiêu tiết kiệm:** Xử lý CRUD mục tiêu tiết kiệm, thực hiện nạp tiền từ ví vào quỹ tiết kiệm (trừ số dư ví và cộng dồn số dư mục tiêu), hoàn tiền từ quỹ tiết kiệm về ví khi thực hiện xóa mục tiêu tiết kiệm. | Trần Minh Vương |

---

## 3. Tích hợp dịch vụ bên thứ 3 (Điểm cộng)
Các dịch vụ đã tích hợp thành công:
* [ ] Email Service
* [x] Google Login
* [ ] Facebook Login
* [ ] Firebase
* [ ] Cloudinary
* [ ] Google Maps API
* [ ] OpenAI API
* [ ] VNPay
* [ ] MoMo
* [x] Chatbot AI
* [ ] Khác: ............................................................

**Mô tả ngắn gọn về các dịch vụ đã tích hợp:**
* **Google Login:** Tích hợp Google OAuth2 cho phép người dùng đăng nhập trực tiếp bằng tài khoản Google. Client gửi `idToken` lên backend để xác thực thông tin và tạo tài khoản/phiên đăng nhập an toàn mà không cần nhập mật khẩu.
* **Chatbot AI (Google Gemini API):** Tích hợp model `gemini-flash-lite-latest` thông qua Google Gemini API để tự động phân tích và tạo ra các đề xuất, lời khuyên tài chính thông minh, giúp người dùng tối ưu hóa chi tiêu cá nhân dựa trên dữ liệu thật.

---

## 4. Tổng hợp đóng góp của các thành viên
| MSSV | Họ và tên | Công việc chính phụ trách | Tỷ lệ đóng góp (%) |
| --- | --- | --- | :---: |
| 4651050329 | Trần Minh Vương | Phát triển module Quản lý Hạn mức ngân sách (Budget) và Quản lý Mục tiêu tiết kiệm (Saving Goals) cả Frontend & Backend. | 25% |
| 4651050387 | Nguyễn Hồ Thế Luân | Thiết lập dự án, Phát triển module Xác thực (Auth, Google Login), Quản lý Danh mục (Category), Cập nhật thông tin cá nhân (User Profile) và Tích hợp dịch vụ AI (Tư vấn tài chính AI & Trợ lý ảo AI Chatbot). | 25% |
| | Trương Thị Thùy Trang | Phát triển module Quản lý Ví (Wallet) và Quản lý Giao dịch (Transaction) cả Frontend & Backend bao gồm CRUD giao dịch, liên kết ví, trừ/cộng số dư ví. | 25% |
| | Ngô Quốc Trung | Phát triển hệ thống Báo cáo & Phân tích (Reports - biểu đồ cột, biểu đồ tròn SVG, xuất file Excel/PDF) và trang Dashboard hiển thị tổng quan tài chính. | 25% |

**Tổng tỷ lệ đóng góp: 100%**

---

## 5. Ma trận đóng góp theo công việc
*(Đánh dấu ✓ vào các nội dung thành viên trực tiếp thực hiện)*

| STT | Chức năng / Module công việc | Trần Minh Vương | Nguyễn Hồ Thế Luân | Trương Thị Thùy Trang | Ngô Quốc Trung |
| :---: | --- | :---: | :---: | :---: | :---: |
| 1 | Thiết kế sơ đồ thực thể liên kết (ERD) và cấu trúc DB | ✓ | ✓ | ✓ | ✓ |
| 2 | Chức năng Đăng nhập & Đăng ký (Auth) | | ✓ | | |
| 3 | Tích hợp Đăng nhập bằng Google Login | | ✓ | | |
| 4 | CRUD Danh mục (Category) | | ✓ | | |
| 5 | Cập nhật thông tin cá nhân (User Profile) | | ✓ | | |
| 6 | Tích hợp Tư vấn tài chính AI (Gemini API) | | ✓ | | |
| 7 | Trợ lý ảo AI Chatbot (Gemini API) | | ✓ | | |
| 8 | CRUD Ví (Wallet) | | | ✓ | |
| 9 | Tính toán tổng số dư các ví | | | ✓ | |
| 10 | CRUD Giao dịch & Chuyển tiền nội bộ | | | ✓ | |
| 11 | Quản lý Hạn mức ngân sách (Budget) | ✓ | | | |
| 12 | Quản lý Mục tiêu tiết kiệm (Saving Goals) | ✓ | | | |
| 13 | Nạp tiền tích lũy và hoàn trả tiền từ quỹ tiết kiệm về ví | ✓ | | | |
| 14 | Báo cáo & Phân tích (SVG, Bar) | | | | ✓ |
| 15 | Xuất báo cáo giao dịch (Excel/PDF) | | | | ✓ |
| 16 | Giao diện Dashboard hiển thị số dư, dòng tiền & thống kê | | | | ✓ |

---

## 6. Tự đánh giá của nhóm
* **Mức độ hoàn thành đề tài:** 100% (đã hoàn thiện toàn bộ các tính năng theo đúng yêu cầu đề tài bao gồm cả các module nâng cao như Giao dịch, Ngân sách và Báo cáo).
* **Chức năng nổi bật nhất:**
  * Tính năng **tư vấn tài chính thông minh bằng AI** (Gemini API) dựa trên dữ liệu người dùng.
  * **Trợ lý ảo AI Chatbot tiện ích:** Hỗ trợ nhập nhanh giao dịch rảnh tay bằng câu thoại ngôn ngữ tự nhiên và tra cứu nhanh ngân sách/số dư/mục tiêu tiết kiệm qua chat.
  * **Tích lũy tài sản qua Mục tiêu tiết kiệm (Saving Goals):** Cho phép chuyển tiền trực tiếp từ ví vào quỹ tích lũy mục tiêu và hoàn tiền linh hoạt khi cần thiết.
  * **Cảnh báo vượt hạn mức ngân sách chi tiêu thông minh** hiển thị ngay lập tức khi thêm giao dịch.
  * **Xuất báo cáo động định dạng Excel và PDF** có bộ lọc thời gian tương ứng với bộ lọc của người dùng trên giao diện.
  * **Đăng nhập bằng tài khoản Google** nhanh chóng, tiện lợi.
* **Chức năng chưa hoàn thành (nếu có):** Không (Đã hoàn thành toàn bộ).
* **Khó khăn lớn nhất:**
  * Đồng bộ luồng xử lý bất đồng bộ của Angular để vẽ biểu đồ và phân chia đoạn biểu đồ tròn SVG tự thiết kế không bị lỗi hiển thị.
  * Quản lý bảo mật CORS và xử lý logic đồng bộ trừ/cộng số dư ví thực tế khi cập nhật hoặc xóa các loại giao dịch (Thu, Chi, Chuyển khoản) phức tạp.
* **Link GitHub:** [https://github.com/luannguyenn889/expense-tracker-app](https://github.com/luannguyenn889/expense-tracker-app)

---

## 7. Cam kết
Chúng tôi cam kết các thông tin trên phản ánh đúng quá trình thực hiện dự án và mức độ tham gia của từng thành viên.

| MSSV | Họ và tên | Ký tên |
| --- | --- | --- |
| 4651050329 | Trần Minh Vương | |
| 4651050387 | Nguyễn Hồ Thế Luân | |
| | Trương Thị Thùy Trang | |
| | Ngô Quốc Trung | |
