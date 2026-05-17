import { Component, OnInit, ChangeDetectorRef } from '@angular/core';  // ← THÊM ChangeDetectorRef
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './transactions.html',
  styleUrl: './transactions.css',
})
export class Transactions implements OnInit {
  wallets: any[] = [];
  transactions: any[] = [];
  
  filter = { startDate: '', endDate: '', type: '', walletId: null as number | null };
  currentPage = 0;
  pageSize = 10;
  totalPages = 1;
  
  showTransferModal = false;
  transferData = {
    fromWalletId: null as number | null,
    toWalletId: null as number | null,
    amount: 0,
    note: ''
  };
  
  showAddModal = false;
  userId = 1;

  constructor(private http: HttpClient, private cdr: ChangeDetectorRef) {}

  ngOnInit() {
    this.loadWallets();
    this.loadTransactions();
  }

  loadWallets() {
    this.http.get(`http://localhost:8080/api/wallets?userId=${this.userId}`)
      .subscribe({
        next: (data: any) => { this.wallets = data; },
        error: (err) => console.error('Lỗi tải ví:', err)
      });
  }

  loadTransactions() {
    let url = `http://localhost:8080/api/transactions?userId=${this.userId}&page=${this.currentPage}&size=${this.pageSize}`;
    if (this.filter.startDate) url += `&startDate=${this.filter.startDate}`;
    if (this.filter.endDate) url += `&endDate=${this.filter.endDate}`;
    if (this.filter.type) url += `&type=${this.filter.type}`;
    if (this.filter.walletId) url += `&walletId=${this.filter.walletId}`;
    
    this.http.get(url).subscribe({
      next: (data: any) => {
        this.transactions = data.content || [];
        this.totalPages = data.totalPages || 1;
      },
      error: (err) => console.error('Lỗi tải giao dịch:', err)
    });
  }

  search() { 
    this.currentPage = 0; 
    this.loadTransactions(); 
  }
  changePage(page: number) { 
    this.currentPage = page; 
    this.loadTransactions(); 
  }

  openAddTransaction() { 
    this.showAddModal = true; 
  }
  closeAddModal() { 
    this.showAddModal = false; 
  }

  openTransferModal() { 
    this.showTransferModal = true; 
  }
  closeTransferModal() { 
    this.showTransferModal = false; 
  }

  isTransferValid(): boolean {
    return !!this.transferData.fromWalletId &&
           !!this.transferData.toWalletId &&
           this.transferData.fromWalletId !== this.transferData.toWalletId &&
           this.transferData.amount > 0;
  }

  doTransfer() {
    if (!this.isTransferValid()) {
      alert('Vui lòng chọn đầy đủ thông tin!');
      return;
    }

    const data = {
      fromWalletId: this.transferData.fromWalletId,
      toWalletId: this.transferData.toWalletId,
      amount: this.transferData.amount,
      note: this.transferData.note,
      transferDate: new Date().toISOString().split('T')[0]
    };

    this.http.post(`http://localhost:8080/api/transactions/transfer?userId=${this.userId}`, data)
      .subscribe({
        next: (res: any) => {
          console.log('Response:', res);
          
          // Đóng popup
          this.showTransferModal = false;
          this.cdr.detectChanges();  
          
          // Hiển thị thông báo
          if (res.success === true || res.message) {
            alert(res.message || 'Chuyển tiền thành công');
          } else {
            alert('Chuyển tiền thành công!');
          }
          
          // Load lại dữ liệu
          this.loadWallets();
          this.loadTransactions();
          
          // Reset form
          this.transferData = {
            fromWalletId: null,
            toWalletId: null,
            amount: 0,
            note: ''
          };
        },
        error: (err) => {
          console.error('Lỗi:', err);
          this.showTransferModal = false;
          this.cdr.detectChanges(); 
          
          let errorMsg = 'Chuyển tiền thất bại!';
          if (err.error && err.error.message) {
            errorMsg = err.error.message;
          }
          alert(errorMsg);
        }
      });
  }
}