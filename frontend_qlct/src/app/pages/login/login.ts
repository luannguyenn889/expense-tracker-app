import {Component, Input, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import {HttpClient} from '@angular/common/http';
import {GoogleSigninButtonDirective, SocialAuthService} from '@abacritt/angularx-social-login';
import {Auth} from '../../services/auth';

import {User} from '../../model/user';
import { ReactiveFormsModule } from '@angular/forms';
@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule, GoogleSigninButtonDirective, ReactiveFormsModule, FormsModule,CommonModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login implements OnInit{
  showPassword = false;
  isLoading = false;

  @Input() public user!: User;


  // Login fields
  email = '';
  password = '';
  rememberMe = false;

  constructor(private router: Router, private http: HttpClient, private authService: SocialAuthService, private auth: Auth) {

  }

// tao login data
  logindata = {
    username: '',
    password: ''

  }
  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  onSubmit() {
  if (!this.logindata.username || !this.logindata.password) {
    alert('Vui lòng nhập đầy đủ thông tin đăng nhập');
    return;
  }
  
  const backend_url = 'http://localhost:8080/api/auth/login';
  this.isLoading = true;

  this.http.post(backend_url, this.logindata).subscribe(
    (response: any) => {
      console.log("Đăng nhập thường thành công", response);
      this.isLoading = false;

      // FIX: Kiểm tra và bóc tách chuẩn dữ liệu từ AuthResponse của Spring Boot
      if (response && response.accessToken && response.user) {
        // Lưu đúng Token thật từ DB và đối tượng user đã tách biệt
        this.auth.setLoginSession(response.accessToken, response.user);
        this.router.navigate(['/']);
      } else {
        // Trường hợp cấu trúc trả về dạng cũ (không bọc trong wrapper)
        // hoặc phòng hờ khi Java trả thẳng Object Users
        const token = response.accessToken || 'normal_login_token';
        const userObj = response.user || response;
        this.auth.setLoginSession(token, userObj);
        this.router.navigate(['/']);
      }
    },
    (error) => {
      console.error("Lỗi đăng nhập:", error);
      this.isLoading = false;
      alert('Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin đăng nhập.');
    }
  );
}
  ngOnInit() {
    // Lắng nghe trạng thái trả về từ thư viện Google
    this.authService.authState.subscribe((googleUser) => {
      if (googleUser) {
        // googleUser có chứa email, tên, avatar... nhưng quan trọng nhất là idToken
        this.verifyTokenWithBackend(googleUser.idToken);
      }
    });
  }

  // Hàm gửi Token xuống Spring Boot
  verifyTokenWithBackend(googleIdToken: string | undefined) {
    this.isLoading = true;
    const backendUrl = 'http://localhost:8080/api/auth/google-login';

    // Gói dữ liệu thành JSON: { "idToken": "chuỗi_token..." }
    const requestBody = { idToken: googleIdToken };

    // Bắn request POST
    this.http.post(backendUrl, requestBody).subscribe({
      next: (response: any) => {
        this.isLoading = false;
        console.log('Backend trả về JWT và User Info thành công:', response);

        // GỌI HÀM SET SESSION TẠI ĐÂY ĐỂ CẬP NHẬT HEADER
        // Backend (AuthResponse) đã trả về sẵn đối tượng 'user' được tách chuỗi chuẩn xác từ Java
        this.auth.setLoginSession(response.accessToken, response.user);

        // 2. Chuyển hướng người dùng vào trang Dashboard
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Đăng nhập thất bại từ phía Backend:', err);
        alert('Đăng nhập thất bại. Vui lòng thử lại!');

        // Xóa thông tin đăng nhập Google bị lỗi để user có thể bấm đăng nhập lại
        this.authService.signOut();
      }
    });
  }
}
