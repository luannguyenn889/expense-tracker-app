import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Router } from '@angular/router';
import { SocialAuthService } from '@abacritt/angularx-social-login'; // Giữ lại từ file 2

@Injectable({
  providedIn: 'root',
})
export class Auth {
  private loggedIn = new BehaviorSubject<boolean>(this.checkToken());
  isLoggedIn$ = this.loggedIn.asObservable();

  private currentUser = new BehaviorSubject<any>(this.getUserFromStorage());
  currentUser$ = this.currentUser.asObservable();

  // Constructor có cả Router và SocialAuthService
  constructor(private router: Router, private socialAuthService: SocialAuthService) { }

  private checkToken(): boolean {
    return !!localStorage.getItem('access_token');
  }

  private getUserFromStorage(): any {
    const userStr = localStorage.getItem('user_info');
    return userStr ? JSON.parse(userStr) : null;
  }

  // GIỮ LẠI TỪ FILE 1: Hàm lấy dữ liệu nhanh cho Dashboard (Sửa lỗi TS2551)
  getCurrentUser(): any {
    return this.currentUser.value || this.getUserFromStorage();
  }
  getCurrentUserId(): number | null {
      const user = this.getCurrentUser();
      return user && user.id ? Number(user.id) : null;
  }
  setLoginSession(token: string, userInfo: any) {
    localStorage.setItem('access_token', token);
    localStorage.setItem('user_info', JSON.stringify(userInfo));

    this.loggedIn.next(true);
    this.currentUser.next(userInfo);
  }

  // GIỮ LẠI TỪ FILE 2: Cập nhật thông tin user (Avatar/Tên) ngay lập tức
  updateCurrentUser(userInfo: any) {
    localStorage.setItem('user_info', JSON.stringify(userInfo));
    this.currentUser.next(userInfo);
  }

  // GIỮ LẠI TỪ FILE 2: Đăng xuất sạch sẽ cả localStorage lẫn Google
  logout() {
    this.socialAuthService.signOut().catch(() => {
        // Bỏ qua lỗi nếu user chưa từng đăng nhập Google
    });

    localStorage.removeItem('access_token');
    localStorage.removeItem('user_info');

    this.loggedIn.next(false);
    this.currentUser.next(null);

    this.router.navigate(['/login']);
  }
}