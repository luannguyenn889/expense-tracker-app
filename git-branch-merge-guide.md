# Hướng dẫn Git Branch Merge

## 1. Tạo một nhánh mới
Trước khi thực hiện merge, bạn nên tạo một nhánh mới để làm việc:
```bash
git checkout -b <ten-nhanh-moi>
```

## 2. Chuyển sang nhánh cần merge
Chuyển sang nhánh mà bạn muốn merge các thay đổi vào:
```bash
git checkout <ten-nhanh-dich>
```

## 3. Merge nhánh nguồn vào nhánh đích
Thực hiện merge nhánh nguồn vào nhánh đích:
```bash
git merge <ten-nhanh-nguon>
```

Nếu có xung đột, Git sẽ thông báo và bạn cần giải quyết xung đột trước khi tiếp tục.

## 4. Giải quyết xung đột (nếu có)
- Mở các file bị xung đột và chỉnh sửa chúng theo ý muốn.
- Sau khi giải quyết xung đột, đánh dấu các file đã được chỉnh sửa:
```bash
git add <ten-file>
```
- Tiếp tục quá trình merge:
```bash
git merge --continue
```

## 5. Kiểm tra trạng thái
Kiểm tra trạng thái của repository để đảm bảo mọi thứ đã ổn:
```bash
git status
```

## 6. Đẩy thay đổi lên remote repository
Sau khi hoàn tất merge, đẩy các thay đổi lên remote repository:
```bash
git push origin <ten-nhanh-dich>
```

## Lưu ý
- Luôn đảm bảo bạn đã pull các thay đổi mới nhất từ remote trước khi merge:
```bash
git pull origin <ten-nhanh-dich>
```
- Kiểm tra lịch sử commit để đảm bảo mọi thứ đúng như mong đợi:
```bash
git log --oneline
```
