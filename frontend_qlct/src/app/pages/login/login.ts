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

  email = '';
  password = '';
  rememberMe = false;

  constructor(private router: Router, private http: HttpClient, private authService: SocialAuthService, private auth: Auth) {
  }

  logindata = {
    username: '',
    password: ''
  }

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  // Chuẩn hóa user object: đảm bảo luôn có cả "id" và "userId"
  // để các component khác (header.ts...) đọc property nào cũng ra giá trị
  private normalizeUser(rawUser: any): any {
    if (!rawUser) return rawUser;
    return {
      ...rawUser,
      id: rawUser.id ?? rawUser.userId,
      userId: rawUser.userId ?? rawUser.id,
    };
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

        if (response && response.accessToken && response.user) {
          const userObj = this.normalizeUser(response.user);
          this.auth.setLoginSession(response.accessToken, userObj);
          this.router.navigate(['/']);
        } else {
          const token = response.accessToken || 'normal_login_token';
          const userObj = this.normalizeUser(response.user || response);
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
    this.authService.authState.subscribe((googleUser) => {
      if (googleUser) {
        this.verifyTokenWithBackend(googleUser.idToken);
      }
    });
  }

  verifyTokenWithBackend(googleIdToken: string | undefined) {
    this.isLoading = true;
    const backendUrl = 'http://localhost:8080/api/auth/google-login';
    const requestBody = { idToken: googleIdToken };

    this.http.post(backendUrl, requestBody).subscribe({
      next: (response: any) => {
        this.isLoading = false;
        console.log('Backend trả về JWT và User Info thành công:', response);

        const userObj = this.normalizeUser(response.user);
        this.auth.setLoginSession(response.accessToken, userObj);

        this.router.navigate(['/']);
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Đăng nhập thất bại từ phía Backend:', err);
        alert('Đăng nhập thất bại. Vui lòng thử lại!');
        this.authService.signOut();
      }
    });
  }
}