import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Wallet } from '../../model/wallet';

@Component({
  selector: 'app-list-wallet',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './list-wallet.html',
  styleUrl: './list-wallet.css'
})
export class ListWallet {
  @Input() wallets: Wallet[] = [];
  @Output() edit = new EventEmitter<Wallet>();
  @Output() delete = new EventEmitter<number>();
}