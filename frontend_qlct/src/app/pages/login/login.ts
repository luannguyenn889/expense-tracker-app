import {Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import {HttpClient} from '@angular/common/http';
import {GoogleSigninButtonDirective, SocialAuthService} from '@abacritt/angularx-social-login';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule, GoogleSigninButtonDirective],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login implements OnInit{
  showPassword = false;
  isLoading = false;
  // tao login data
  logindata = {
    username: '',
    password:'',

  }
  // Login fields
  email = '';
  password = '';
  rememberMe = false;

  constructor(private router: Router, private http: HttpClient,private authService: SocialAuthService ) {}





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
    // Bắn request POST mang theo object loginData (Angular tự động biến nó thành JSON)
    this.http.post(backend_url, this.logindata).subscribe(
      (response) => {
        // Xử lý phản hồi từ server
        console.log(response);
        this.isLoading = false;
        this.router.navigate(['/']);
      },
      (error) => {
        // Xử lý lỗi
        console.error(error);
        this.isLoading = false;
        alert('Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin đăng nhập.');
      }
    );


    this.isLoading = true;

    // Simulate API call
    setTimeout(() => {
      this.isLoading = false;
      this.router.navigate(['/']);
    }, 1500);
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
        console.log('Backend trả về JWT thành công:', response);

        // 1. Lưu JWT của hệ thống (do Spring Boot trả về) vào LocalStorage
        localStorage.setItem('access_token', response.accessToken);

        // 2. Chuyển hướng người dùng vào trang Dashboard
        this.router.navigate(['/dashboard']);
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
