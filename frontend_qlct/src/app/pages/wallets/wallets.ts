import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ListWallet } from '../../wallets/list-wallet/list-wallet';
import { AddWallet } from '../../wallets/add-wallet/add-wallet';
import { EditWallet } from '../../wallets/edit-wallet/edit-wallet';
import { WalletService } from '../../services/wallet-service';

@Component({
  selector: 'app-wallets',
  standalone: true,
  imports: [CommonModule, ListWallet, AddWallet, EditWallet],
  templateUrl: './wallets.html',
  styleUrl: './wallets.css'
})
export class Wallets implements OnInit {
  wallets: any[] = [];
  isLoading = true;
  showAddModal = false;
  showEditModal = false;
  selectedWallet: any = null;

  constructor(
    private walletService: WalletService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadWallets();
    if (sessionStorage.getItem('needRefreshWallets')) {
      sessionStorage.removeItem('needRefreshWallets');
      this.loadWallets();
    }
  }
  // Load danh sách tất cả các ví của user
  loadWallets() {
    this.isLoading = true;
    this.cdr.detectChanges();
    
    this.walletService.getWallets().subscribe({
      next: (data) => {
        this.wallets = data || [];
        this.isLoading = false;
        this.cdr.detectChanges();
        console.log('Wallets loaded:', this.wallets);
      },
      error: (err) => {
        console.error('Lỗi tải ví:', err);
        this.wallets = [];
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  getTotalBalance(): number {
    return this.wallets.reduce((sum, w) => sum + (w.balance || 0), 0);
  }

  openAddModal() { this.showAddModal = true; }
  closeAddModal() { this.showAddModal = false; }
  
  openEditModal(wallet: any) {
    this.selectedWallet = wallet;
    this.showEditModal = true;
  }
  closeEditModal() { this.showEditModal = false; }

  deleteWallet(id: number) {
    this.walletService.hasTransactions(id).subscribe({
      next: (response: any) => {
        if (response.hasTransactions) {
          if (confirm('Ví này đã có giao dịch.\n\nBạn có muốn chuyển sang trạng thái NGƯNG HOẠT ĐỘNG (vẫn giữ lịch sử) không?')) {
            this.walletService.deleteWallet(id).subscribe({
              next: () => {
                this.loadWallets();
                alert('Ví đã được chuyển sang trạng thái ngưng hoạt động!');
              },
              error: () => alert('Thao tác thất bại!')
            });
          }
          return;
        }
        
        if (confirm('Xóa ví này?')) {
          this.walletService.deleteWallet(id).subscribe({
            next: () => {
              this.loadWallets();
              alert('Xóa ví thành công!');
            },
            error: () => alert('Xóa ví thất bại!')
          });
        }
      },
      error: () => {
        if (confirm('Xóa ví này?')) {
          this.walletService.deleteWallet(id).subscribe({
            next: () => {
              this.loadWallets();
              alert('Xóa ví thành công!');
            },
            error: () => alert('Xóa ví thất bại!')
          });
        }
      }
    });
  }

  onWalletAdded() {
    this.closeAddModal();
    this.loadWallets();
  }

  onWalletUpdated() {
    this.closeEditModal();
    this.loadWallets();
  }
}