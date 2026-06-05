import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WalletService } from '../../services/wallet-service';

@Component({
  selector: 'app-add-wallet',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './add-wallet.html',
  styleUrl: './add-wallet.css'
})
export class AddWallet {
  @Output() close = new EventEmitter<void>();
  @Output() added = new EventEmitter<void>();

  nameError = false;
  newWallet = {
    name: '',
    balance: 0,
    description: '',
    currency: 'VND'
  };

  constructor(private walletService: WalletService) {}

  save() {
    if (!this.newWallet.name || !this.newWallet.name.trim()) {
      this.nameError = true;
      return;
    }
    this.nameError = false;

    if (!this.newWallet.balance || this.newWallet.balance < 0) {
      this.newWallet.balance = 0;
    }

    this.walletService.addWallet(this.newWallet).subscribe({
      next: () => {
        alert('Thêm ví thành công!');
        this.added.emit();
        this.close.emit();
      },
      error: () => alert('Thêm ví thất bại!')
    });
  }

  cancel() {
    this.close.emit();
  }
}