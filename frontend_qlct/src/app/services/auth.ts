import { Injectable } from '@angular/core';
import {BehaviorSubject} from 'rxjs';
import { SocialAuthService } from '@abacritt/angularx-social-login';
import {Router} from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class Auth {
  // BehaviorSubject lưu trạng thái đăng nhập, khởi tạo dựa trên việc có token trong localStorage hay không
  private loggedIn = new BehaviorSubject<boolean>(this.checkToken());

  // Biến này để các component khác (như Header) subscribe (lắng nghe)
  isLoggedIn$ = this.loggedIn.asObservable();

  // (Tùy chọn) Lưu thêm thông tin user như email, tên, avatar...
  private currentUser = new BehaviorSubject<any>(this.getUserFromStorage());
  currentUser$ = this.currentUser.asObservable();

  constructor(private router: Router, private socialAuthService: SocialAuthService) { }

  // Hàm kiểm tra xem token có tồn tại không (để giữ trạng thái khi F5 trang)
  private checkToken(): boolean {
    return !!localStorage.getItem('access_token');
  }

  private getUserFromStorage(): any {
    const userStr = localStorage.getItem('user_info');
    return userStr ? JSON.parse(userStr) : null;
  }

  // Hàm này sẽ được gọi bên trong LoginComponent khi API trả về thành công
  setLoginSession(token: string, userInfo: any) {
    localStorage.setItem('access_token', token);
    localStorage.setItem('user_info', JSON.stringify(userInfo)); // Lưu thông tin user

    this.loggedIn.next(true); // Phát tín hiệu: Đã đăng nhập!
    this.currentUser.next(userInfo); // Phát tín hiệu: Thông tin user đây!
  }

  // Hàm gọi khi nhấn Đăng xuất ở Header
  logout() {
    // Đăng xuất khỏi Google nếu đang dùng tài khoản Google
    this.socialAuthService.signOut().catch(() => {
        // Bỏ qua lỗi nếu user chưa từng đăng nhập Google
    });

    localStorage.removeItem('access_token');
    localStorage.removeItem('user_info');

    this.loggedIn.next(false); // Phát tín hiệu: Đã thoát!
    this.currentUser.next(null);

    this.router.navigate(['/login']); // Đẩy về trang đăng nhập
  }
  
}
