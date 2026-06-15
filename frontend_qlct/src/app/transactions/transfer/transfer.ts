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

  /**
   * Kiểm tra tính hợp lệ của dữ liệu form chuyển tiền
   */
  isValid(): boolean {
    return !!this.transferData.fromWalletId &&
           !!this.transferData.toWalletId &&
           this.transferData.fromWalletId !== this.transferData.toWalletId &&
           this.transferData.amount > 0;
  }

  /**
   * Thực hiện gọi API chuyển tiền liên ví
   */
  transfer() {
    if (!this.isValid()) {
      alert('Vui lòng điền đầy đủ và chính xác thông tin chuyển tiền!');
      return;
    }

    this.transactionService.transfer(this.transferData).subscribe({
      next: (res: any) => {
        alert(res.message || 'Chuyển tiền thành công!');
        this.completed.emit(); // Thông báo cho component cha tải lại dữ liệu ví/giao dịch
        this.close.emit();     // Đóng modal/form chuyển tiền
      },
      error: (err) => {
        // Đọc thông báo lỗi trả về từ Backend (Khớp với các Exception trả về từ Java Controller)
        alert(err.error?.message || err.error || 'Chuyển tiền thất bại!');
      }
    });
  }

  /**
   * Hủy bỏ thao tác
   */
  cancel() {
    this.close.emit();
  }
}