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
  userId = 1;

  // Popup thêm ví
  showAddModal = false;
  nameError = false;

  newWallet = {
    name: '',
    balance: 0,
    description: '',
    currency: 'VND'
  };

  // Popup sửa ví
  editingWallet: any = null;

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

  getTotalBalance(): number {
    return this.wallets.reduce((sum, wallet) => sum + (wallet.balance || 0), 0);
  }

  // Mở popup thêm ví
  openAddModal() {
    this.showAddModal = true;
    this.nameError = false;
    this.newWallet = {
      name: '',
      balance: 0,
      description: '',
      currency: 'VND'
    };
  }

  // Đóng popup thêm ví
  closeAddModal() {
    this.showAddModal = false;
  }

  createWallet() {
    // Kiểm tra tên ví không được để trống
    if (!this.newWallet.name || !this.newWallet.name.trim()) {
      this.nameError = true;
      this.cdr.detectChanges();
      return;
    }
    this.nameError = false;

    // Số dư mặc định = 0 nếu không nhập
    if (!this.newWallet.balance || this.newWallet.balance < 0) {
      this.newWallet.balance = 0;
    }

    this.http.post(`http://localhost:8080/api/wallets?userId=${this.userId}`, {
      name: this.newWallet.name,
      balance: this.newWallet.balance,
      description: this.newWallet.description,
      currency: this.newWallet.currency
    }).subscribe({
      next: () => {
        this.loadWallets();
        this.closeAddModal();
        this.cdr.detectChanges();
        alert('Thêm ví thành công!');
      },
      error: (err) => {
        console.error('Lỗi tạo ví:', err);
        alert('Thêm ví thất bại!');
      }
    });
  }

  editWallet(wallet: any) {
    this.editingWallet = {
      id: wallet.id,
      name: wallet.name,
      balance: wallet.balance,
      description: wallet.description
    };
  }

  updateWallet() {
    if (!this.editingWallet.name || !this.editingWallet.name.trim()) {
      alert('Tên ví không được để trống!');
      return;
    }

    this.http.put(`http://localhost:8080/api/wallets/${this.editingWallet.id}?userId=${this.userId}`, {
      name: this.editingWallet.name,
      description: this.editingWallet.description
    }).subscribe({
      next: () => {
        this.loadWallets();
        this.editingWallet = null;
        this.cdr.detectChanges();
        alert('Sửa ví thành công!');
      },
      error: (err) => {
        console.error('Lỗi sửa ví:', err);
        alert('Sửa ví thất bại!');
      }
    });
  }

  cancelEdit() {
    this.editingWallet = null;
  }

  deleteWallet(id: number) {
    // Kiểm tra ví có giao dịch không
    this.http.get(`http://localhost:8080/api/wallets/${id}/has-transactions?userId=${this.userId}`)
      .subscribe({
        next: (response: any) => {
          if (response.hasTransactions) {
            alert('Ví này đã có giao dịch! Vui lòng xóa hoặc chuyển giao dịch trước khi xóa ví.');
            return;
          }
          
          // Nếu chưa có giao dịch, hỏi xác nhận
          if (confirm('Bạn có chắc chắn muốn xóa ví này không?')) {
            this.http.delete(`http://localhost:8080/api/wallets/${id}?userId=${this.userId}`)
              .subscribe({
                next: () => {
                  this.loadWallets();
                  alert('Xóa ví thành công!');
                },
                error: (err) => {
                  console.error('Lỗi xóa ví:', err);
                  alert('Xóa ví thất bại!');
                }
              });
          }
        },
        error: (err) => {
          console.error('Lỗi kiểm tra giao dịch:', err);
          // Nếu chưa có API kiểm tra, vẫn cho xóa nhưng cảnh báo
          if (confirm('Bạn có chắc chắn muốn xóa ví này không? Hành động này không thể hoàn tác!')) {
            this.http.delete(`http://localhost:8080/api/wallets/${id}?userId=${this.userId}`)
              .subscribe({
                next: () => {
                  this.loadWallets();
                  alert('Xóa ví thành công!');
                },
                error: (err) => {
                  console.error('Lỗi xóa ví:', err);
                  alert('Xóa ví thất bại!');
                }
              });
          }
        }
      });
  }
}