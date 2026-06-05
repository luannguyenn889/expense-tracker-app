import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WalletService } from '../../services/wallet-service';

@Component({
  selector: 'app-edit-wallet',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './edit-wallet.html',
  styleUrl: './edit-wallet.css'
})
export class EditWallet {
  @Input() wallet: any = null;
  @Output() close = new EventEmitter<void>();
  @Output() updated = new EventEmitter<void>();

  editingWallet: any = null;

  constructor(private walletService: WalletService) {}

  ngOnChanges() {
    if (this.wallet) {
      this.editingWallet = { ...this.wallet };
    }
  }

  update() {
    if (!this.editingWallet.name || !this.editingWallet.name.trim()) {
      alert('Tên ví không được để trống!');
      return;
    }

    this.walletService.updateWallet(this.editingWallet.id, {
      name: this.editingWallet.name,
      description: this.editingWallet.description
    }).subscribe({
      next: () => {
        alert('Sửa ví thành công!');
        this.updated.emit();
        this.close.emit();
      },
      error: () => alert('Sửa ví thất bại!')
    });
  }

  cancel() {
    this.close.emit();
  }
}