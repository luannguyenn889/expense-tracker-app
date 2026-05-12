import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import {HttpClient} from '@angular/common/http';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
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

  constructor(private router: Router, private http: HttpClient  ) {}





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
}
