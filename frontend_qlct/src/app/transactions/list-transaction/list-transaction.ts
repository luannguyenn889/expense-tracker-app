import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-list-transaction',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './list-transaction.html',
  styleUrl: './list-transaction.css'
})
export class ListTransaction {
  @Input() transactions: any[] = [];
  @Input() wallets: any[] = [];       
  @Input() currentPage: number = 0;
  @Input() totalPages: number = 1;
  @Output() pageChange = new EventEmitter<number>();
  @Output() edit = new EventEmitter<any>();
  @Output() delete = new EventEmitter<number>();

  changePage(page: number) {
    if (page >= 0 && page < this.totalPages) {
      this.pageChange.emit(page);
    }
  }

  getWalletDisplayName(walletId: number): string {
    if (!walletId) return 'Không xác định';
    
    const wallet = this.wallets.find(w => w.id === walletId);
    
    if (!wallet) return 'Ví đã bị xóa';
    
    if (wallet.status === 'INACTIVE') {
      return `${wallet.name} (Đã ngưng hoạt động)`;
    }
    
    return wallet.name;
  }
}