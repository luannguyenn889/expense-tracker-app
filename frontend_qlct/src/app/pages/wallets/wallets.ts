import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-wallets',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './wallets.html',
  styleUrl: './wallets.css',
})
export class Wallets implements OnInit {
  wallets: any[] = [];
  isLoading = false;
  userId = 1; // Tạm thời hardcode, sau lấy từ auth service

  // Form thêm ví
  newWallet = {
    name: '',
    balance: 0,
    description: ''
  };

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.loadWallets();
  }

  loadWallets() {
    this.isLoading = true;
    this.http.get(`http://localhost:8080/api/wallets?userId=${this.userId}`)
      .subscribe({
        next: (data: any) => {
          this.wallets = data;
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Lỗi tải ví:', err);
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
  }

  createWallet() {
    this.http.post(`http://localhost:8080/api/wallets?userId=${this.userId}`, this.newWallet)
      .subscribe({
        next: () => {
          this.loadWallets();
          this.newWallet = { name: '', balance: 0, description: '' };
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Lỗi tạo ví:', err)
      });
  }

  deleteWallet(id: number) {
    if (confirm('Xóa ví này?')) {
      this.http.delete(`http://localhost:8080/api/wallets/${id}?userId=${this.userId}`)
        .subscribe({
          next: () => this.loadWallets(),
          error: (err) => console.error('Lỗi xóa ví:', err)
        });
    }
  }
  scrollToForm() {
    document.querySelector('.add-wallet-form')?.scrollIntoView({ 
        behavior: 'smooth' 
    });
    }
}