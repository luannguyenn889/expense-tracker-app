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


  // Dữ llieu đăng nhập thường
  email = '';
  password = '';
  rememberMe = false;

  constructor(private router: Router, private http: HttpClient, private authService: SocialAuthService, private auth: Auth) {

  }

// Tạo một object để chứa dữ liệu đăng nhập thường, sẽ được Angular tự động bind với form
  logindata = {
    username: '',
    password: ''

  }
  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  onSubmit() {
    if(!this.logindata.username || !this.logindata.password){
      alert('Vui lòng nhập đầy đủ thông tin đăng nhập');
      return;
    }
    // URL cua Spring Boot
    const backend_url = 'http://localhost:8080/api/auth/login';

    this.isLoading = true;

    // Bắn request POST mang theo object loginData (Angular tự động biến nó thành JSON)
    this.http.post(backend_url, this.logindata).subscribe(
      (response: any) => {
        // Xử lý phản hồi từ server
        console.log("Đăng nhập thường thành công", response);
        this.isLoading = false;

        // GỌI HÀM SET SESSION TẠI ĐÂY ĐỂ CẬP NHẬT HEADER
        // Truyền một token giả định và thông tin user vừa nhận được từ backend
        this.auth.setLoginSession('normal_login_token', response);

        this.router.navigate(['/']);
      },
      (error) => {
        // Xử lý lỗi
        console.error(error);
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
