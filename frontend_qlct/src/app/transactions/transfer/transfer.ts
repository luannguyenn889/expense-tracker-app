import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TransactionService } from '../../services/transaction-service';

@Component({
  selector: 'app-transfer',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './transfer.html',
  styleUrl: './transfer.css'
})
export class Transfer {
  @Input() wallets: any[] = [];
  @Output() close = new EventEmitter<void>();
  @Output() completed = new EventEmitter<void>();

  transferData = {
    fromWalletId: null as number | null,
    toWalletId: null as number | null,
    amount: 0,
    note: ''
  };

  constructor(private transactionService: TransactionService) {}

  isValid(): boolean {
    return !!this.transferData.fromWalletId &&
           !!this.transferData.toWalletId &&
           this.transferData.fromWalletId !== this.transferData.toWalletId &&
           this.transferData.amount > 0;
  }

  transfer() {
    if (!this.isValid()) {
      alert('Vui lòng chọn đầy đủ thông tin!');
      return;
    }

    this.transactionService.transfer(this.transferData).subscribe({
      next: (res: any) => {
        alert(res.message || 'Chuyển tiền thành công!');
        this.completed.emit();
        this.close.emit();
      },
      error: (err) => {
        alert(err.error?.message || err.error || 'Chuyển tiền thất bại!');
      }
    });
  }

  cancel() {
    this.close.emit();
  }
}