import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { TransactionService } from '../../services/transaction-service';


@Component({
  selector: 'app-add-transaction',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './add-transaction.html',
  styleUrl: './add-transaction.css'
})
export class AddTransaction {
  @Input() wallets: any[] = [];
  @Input() categories: any[] = [];
  @Output() close = new EventEmitter<void>();
  @Output() added = new EventEmitter<any>();

  transactionData = {
    type: 'EXPENSE',
    amount: 0,
    transactionDate: new Date().toISOString().split('T')[0],
    categoryId: null as string | null,
    walletId: null as number | null,
    note: ''
  };

  constructor(private transactionService: TransactionService) {}

  save() {
    if (!this.transactionData.walletId) {
      alert('Vui lòng chọn ví!');
      return;
    }
    if (!this.transactionData.categoryId) {
      alert('Vui lòng chọn danh mục!');
      return;
    }
    if (this.transactionData.amount <= 0) {
      alert('Số tiền phải lớn hơn 0!');
      return;
    }

    this.transactionService.addTransaction(this.transactionData).subscribe({
      next: () => {
        this.added.emit(this.transactionData);
        this.close.emit();
      },
      error: (err) => {
        alert(err.error || 'Thêm giao dịch thất bại!');
      }
    });
  }

  cancel() {
    this.close.emit();
  }
}
