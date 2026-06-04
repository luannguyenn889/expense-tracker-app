# Hướng dẫn sử dụng Git

## 1. Giới thiệu cơ bản
Git là hệ thống quản lý phiên bản phân tán. Dùng Git để theo dõi thay đổi mã nguồn, làm việc nhóm và lưu lịch sử dự án.

## 2. Trạng thái và theo dõi
- `git status`: kiểm tra thay đổi hiện tại.
- `git add <file>`: đưa file vào staging area để chuẩn bị commit.
- `git add .`: thêm tất cả thay đổi (mới, sửa, xóa) vào staging.
- `git restore <file>`: bỏ thay đổi chưa commit trong working directory.
- `git restore --staged <file>`: bỏ file khỏi staging nhưng giữ thay đổi ở working directory.

## 3. Commit thay đổi
- `git commit -m "Thông điệp commit"`: tạo commit với thông điệp mô tả.
- `git commit -a -m "Thông điệp"`: thêm tất cả file đã được theo dõi và commit luôn (không thêm file mới chưa tracked).
- `git commit --amend`: sửa commit cuối cùng nếu chưa push.

## 4. Làm việc với remote
- `git remote -v`: xem remote hiện có.
- `git push`: đẩy commit lên remote hiện tại.
- `git push origin main`: đẩy branch `main` lên remote `origin`.
- `git pull`: tải và gộp thay đổi từ remote vào branch hiện tại.
- `git fetch`: tải thay đổi từ remote nhưng không merge.

## 5. Nhánh (branch)
- `git branch`: liệt kê các nhánh cục bộ.
- `git branch <tên-nhánh>`: tạo nhánh mới.
- `git checkout <tên-nhánh>`: chuyển sang nhánh khác.
- `git switch <tên-nhánh>`: tương tự `checkout` để chuyển nhánh.
- `git switch -c <tên-nhánh>`: tạo và chuyển ngay sang nhánh mới.
- `git merge <tên-nhánh>`: gộp nhánh vào nhánh hiện tại.

## 6. Xem lịch sử và khác biệt
- `git log`: xem lịch sử commit.
- `git log --oneline --graph --all`: xem lịch sử ngắn gọn theo đồ thị.
- `git diff`: xem khác biệt chưa staged.
- `git diff --staged`: xem khác biệt đã staged.
- `git show <commit>`: xem chi tiết một commit.

## 7. Tạo mới repository
- `git init`: khởi tạo repository Git trong thư mục hiện tại.
- `git clone <url>`: sao chép repository từ remote về máy.

## 8. Quản lý file và `.gitignore`
- `.gitignore` dùng để liệt kê file/thư mục Git không theo dõi.
- Ví dụ: `node_modules/`, `target/`, `*.log`.
- Thêm file đã bị `.gitignore` vào Git sau khi đã bỏ theo dõi: `git rm --cached <file>` rồi commit.

## 9. Hoàn tác và sửa lỗi
- `git restore <file>`: quay lại phiên bản file trong HEAD.
- `git restore --staged <file>`: bỏ file khỏi staging.
- `git reset HEAD <file>`: tương đương `restore --staged` với Git cũ.
- `git reset --soft <commit>`: dịch HEAD về commit, giữ thay đổi trong staging.
- `git reset --mixed <commit>`: giữ thay đổi trong working directory, bỏ staging.
- `git reset --hard <commit>`: xóa mọi thay đổi chưa commit, cực kỳ cẩn thận.

## 10. Lưu tạm thay đổi
- `git stash`: lưu tạm thay đổi chưa commit.
- `git stash pop`: lấy lại thay đổi đã stash và xóa stash đó.
- `git stash apply`: lấy lại thay đổi đã stash nhưng giữ stash.
- `git stash list`: xem danh sách stash.

## 11. Quy trình làm việc phổ biến
1. `git pull` để đồng bộ từ remote.
2. `git checkout -b <feature>` hoặc `git switch -c <feature>` để tạo nhánh mới.
3. Chỉnh sửa file.
4. `git add <file>`.
5. `git commit -m "Mô tả rõ ràng"`.
6. `git push origin <feature>` để đẩy lên remote.

## 12. Lời khuyên
- Viết thông điệp commit ngắn gọn nhưng rõ ràng.
- Commit theo từng bước chức năng nhỏ.
- Dùng `.gitignore` để tránh đưa file tạm, file biên dịch, hoặc dữ liệu riêng tư lên Git.
- Trước khi push, luôn kiểm tra `git status` và `git diff`.

## 13. Ví dụ cho dự án hiện tại
Trong repository này bạn có:
- `backend_qlct/`: phần backend Java.
- `fronend_qlct/`: phần frontend Angular.

Workflow thường dùng:
- `git add .gitignore backend_qlct/ fronend_qlct/`
- `git commit -m "Add backend and frontend project files"`
- `git push`

---

File này tổng hợp các lệnh Git cơ bản để bạn dùng trong hầu hết trường hợp.
