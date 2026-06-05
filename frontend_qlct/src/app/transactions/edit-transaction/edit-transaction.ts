import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { TransactionService } from '../../services/transactionService';


@Component({
  selector: 'app-edit-transaction',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './edit-transaction.html',
  styleUrl: './edit-transaction.css'
})
export class EditTransaction implements OnInit {
  @Input() transaction: any = null;
  @Input() wallets: any[] = [];
  @Input() categories: any[] = [];
  @Output() close = new EventEmitter<void>();
  @Output() updated = new EventEmitter<void>();

  formData = {
    type: '',
    amount: 0,
    transactionDate: '',
    categoryId: null as string | null,
    walletId: null as number | null,
    note: ''
  };

  isTransfer: boolean = false;

  constructor(private transactionService: TransactionService) {}

  ngOnInit() {
    if (this.transaction) {
      this.isTransfer = this.transaction.type === 'TRANSFER';
      this.formData = {
        type: this.transaction.type,
        amount: this.transaction.amount,
        transactionDate: this.transaction.transactionDate,
        categoryId: this.transaction.categoryId || null,
        walletId: this.transaction.walletId,
        note: this.transaction.note || ''
      };
    }
  }

  save() {
    if (!this.formData.walletId) {
      alert('Vui lòng chọn ví!');
      return;
    }
    if (!this.isTransfer && !this.formData.categoryId) {
      alert('Vui lòng chọn danh mục!');
      return;
    }
    if (this.formData.amount <= 0) {
      alert('Số tiền phải lớn hơn 0!');
      return;
    }

    const dataToSend = this.isTransfer 
      ? { ...this.formData, categoryId: null }
      : this.formData;

    this.transactionService.updateTransaction(this.transaction.id, dataToSend).subscribe({
      next: () => {
        alert('Sửa giao dịch thành công!');
        this.updated.emit();
        this.close.emit();
      },
      error: (err) => {
        alert(err.error || 'Sửa giao dịch thất bại!');
      }
    });
  }

  cancel() {
    this.close.emit();
  }
}