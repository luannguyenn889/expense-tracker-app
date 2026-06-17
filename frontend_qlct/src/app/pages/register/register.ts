import {Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import {HttpClient} from '@angular/common/http';

@Component({
  selector: 'app-register',
  imports: [CommonModule, FormsModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register implements OnInit{
  showPassword = false;
  showConfirmPassword = false;
  isLoading = false;

  // Register fields
  // fullName = '';
  // email = '';
  // password = '';
  confirmPassword = '';
  // username = '';
  // firstname = '';
  // lastname = '';
  // dob = '';
  loadData = {
    password : '',

    username : '',
    firstname : '',
    lastname : '',
    dob : '',


  }
  constructor(private router: Router, private http: HttpClient) {}

  ngOnInit(): void {
        throw new Error("Method not implemented.");
    }
  // Toggle hiển thị mật khẩu
  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }
  // Toggle hiển thị xác nhận mật khẩu
  toggleConfirmPasswordVisibility() {
    this.showConfirmPassword = !this.showConfirmPassword;
  }
  
  onSubmit() {
    if (this.loadData.password !== this.confirmPassword) {
      alert('Mật khẩu và xác nhận mật khẩu không khớp!');
      return;
    }
    this.isLoading = true;
    // URL cua Spring Boot

    const backend_url = 'http://localhost:8080/api/auth/register';
    // Bắn request POST mang theo object loginData (Angular tự động biến nó thành JSON)
    this.http.post(backend_url, this.loadData).subscribe(
      (response) => {
        // Xử lý phản hồi từ server
        console.log(response);
        this.isLoading = false;
        this.router.navigate(['/']);
        alert('Đăng ky thành công');
      },
      (error) => {
        // Xử lý lỗi
        console.error(error);
        this.isLoading = false;
        alert('Đăng ky thất bại. Vui lòng kiểm tra lại thông tin ');
      }
    );

    // Simulate API call
    setTimeout(() => {
      this.isLoading = false;
      this.router.navigate(['/login']);
    }, 1500);
  }
}
