# Hướng Dẫn Git: Quy Trình Đẩy Code Từ Nhánh Luan Lên Nhánh Dev

Tài liệu này hướng dẫn chi tiết các bước sử dụng Git để commit code từ nhánh cá nhân (`Luan`) và push lên nhánh dùng chung (`dev`) một cách an toàn, hạn chế tối đa xung đột (conflict).

---

## 1. Khởi tạo & Cập nhật nhánh trước khi làm việc

Trước khi bắt đầu code tính năng mới trong ngày, bạn luôn cần đảm bảo nhánh cá nhân của mình đang có code mới nhất từ nhánh `dev`.

### Bước 1: Di chuyển sang nhánh `dev`
```bash
git checkout dev
```

### Bước 2: Kéo code mới nhất của nhánh `dev` từ server (remote) về máy
```bash
git pull origin dev
```
*(Nếu có thông báo lỗi chưa commit code ở nhánh hiện tại, hãy xem mục 2. Commit code trước)*

### Bước 3: Di chuyển trở lại nhánh `Luan`
```bash
git checkout Luan
```
*(Nếu nhánh `Luan` chưa tồn tại trên máy bạn, hãy tạo mới: `git checkout -b Luan`)*

### Bước 4: Gộp (Merge) code mới nhất từ nhánh `dev` vào nhánh `Luan`
```bash
git merge dev
```
*Việc này giúp nhánh của bạn đồng bộ với code của mọi người, tránh conflict khi push lên sau này.*

---

## 2. Commit code sau khi làm xong tính năng

Sau khi bạn code xong một tính năng hoặc fix xong một lỗi trên nhánh `Luan`:

### Bước 1: Kiểm tra các file đã thay đổi
```bash
git status
```
*Lệnh này sẽ liệt kê các file bạn vừa sửa (màu đỏ).*

### Bước 2: Thêm các file thay đổi vào staging area (Chuẩn bị commit)
Để thêm **tất cả** các file:
```bash
git add .
```
*(Hoặc thêm từng file cụ thể: `git add ten-file.java`)*

### Bước 3: Ghi nhận thay đổi (Commit)
Bạn cần viết lời nhắn (message) rõ ràng để mọi người biết bạn vừa làm gì:
```bash
git commit -m "feat(auth): Thêm chức năng đăng nhập Google"
```
*Gợi ý chuẩn viết commit (Conventional Commits):*
- `feat: ...` (Thêm tính năng mới)
- `fix: ...` (Sửa lỗi)
- `docs: ...` (Cập nhật tài liệu .md)
- `style: ...` (Chỉnh sửa format, CSS, giao diện)
- `refactor: ...` (Tối ưu code, không làm thay đổi logic)

---

## 3. Push code và gộp vào nhánh Dev (Quy trình chuẩn)

Sau khi commit xong ở nhánh `Luan`, đây là luồng chuẩn nhất để đưa code lên nhánh `dev` mà không sợ làm hỏng code của team.

### Bước 1: Push code của nhánh `Luan` lên server (GitHub/GitLab)
```bash
git push origin Luan
```
*Nếu đây là lần đầu tiên bạn push nhánh này lên server, git có thể yêu cầu lệnh: `git push --set-upstream origin Luan`*

### Bước 2: Cập nhật lại nhánh `dev` trên máy bạn một lần nữa (Bắt buộc)
*Vì trong lúc bạn code, có thể người khác đã push code của họ lên `dev` rồi.*
```bash
git checkout dev
git pull origin dev
```

### Bước 3: Gộp (Merge) code từ nhánh `Luan` vào nhánh `dev`
```bash
git merge Luan
```

**⚠️ XỬ LÝ CONFLICT (NẾU CÓ):**
Nếu Git báo lỗi `CONFLICT (content): Merge conflict in ...`:
1. Mở IDE (Android Studio / VS Code) lên.
2. Tìm đến các file bị lỗi (IDE thường bôi đỏ).
3. Tìm các dòng có dấu `<<<<<<< HEAD` và `>>>>>>> Luan`.
4. Quyết định giữ lại code nào, xóa code nào (hoặc giữ cả hai). Xóa các dấu `<<<`, `===`, `>>>` đi.
5. Sau khi sửa xong, lưu lại và gõ lệnh:
   ```bash
   git add .
   git commit -m "fix: Resolve merge conflict between Luan and dev"
   ```

### Bước 4: Push code từ nhánh `dev` (đã có code của Luan) lên server
```bash
git push origin dev
```

### Bước 5: Quay lại nhánh Luan để tiếp tục làm việc
```bash
git checkout Luan
```

---

## Tóm tắt luồng lệnh (Quick Flow)

```bash
# 1. Ở nhánh Luan, commit code của bạn
git add .
git commit -m "tin-nhan-commit"

# 2. Qua nhánh dev, lấy code mới nhất về
git checkout dev
git pull origin dev

# 3. Gộp code của Luan vào dev
git merge Luan
# (Xử lý conflict nếu có)

# 4. Push dev lên server
git push origin dev

# 5. Về lại Luan làm tiếp
git checkout Luan
```