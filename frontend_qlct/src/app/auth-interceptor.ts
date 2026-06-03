import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // 1. Lấy token đang lưu trong trình duyệt
  const token = localStorage.getItem('access_token');

  // 2. Nếu có token, "độ" lại cái Request bằng cách dán thêm nhãn Authorization
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  // 3. Cho phép Request tiếp tục bay tới Backend
  return next(req);
};
