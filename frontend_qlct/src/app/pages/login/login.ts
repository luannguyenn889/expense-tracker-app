import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  isLoginMode = true;
  showPassword = false;
  showConfirmPassword = false;
  isLoading = false;

  // Login fields
  email = '';
  password = '';
  rememberMe = false;

  // Register fields
  fullName = '';
  confirmPassword = '';

  constructor(private router: Router) {}

  toggleMode() {
    this.isLoginMode = !this.isLoginMode;
    this.resetForm();
  }

  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  toggleConfirmPasswordVisibility() {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  resetForm() {
    this.email = '';
    this.password = '';
    this.confirmPassword = '';
    this.fullName = '';
    this.rememberMe = false;
    this.showPassword = false;
    this.showConfirmPassword = false;
  }

  onSubmit() {
    this.isLoading = true;

    // Simulate API call
    setTimeout(() => {
      this.isLoading = false;
      if (this.isLoginMode) {
        this.router.navigate(['/']);
      } else {
        // After registration, switch to login
        this.isLoginMode = true;
        this.resetForm();
      }
    }, 1500);
  }
}
