import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TransactionService } from '../../services/transactionService';
import { Auth } from '../../services/auth';
import { Subject, debounceTime } from 'rxjs';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './transactions.html',
  styleUrls: ['./transactions.css']
})
export class Transactions implements OnInit, OnDestroy {
  userId!: number;
  currentPage = 0;
  size = 10;
  totalPages = 0;

  transactions: any[] = [];
  wallets: any[] = [];
  notifications: any[] = [];
  comparisonData: any = null;

  isTransferModalOpen = false;
  isAddTransactionModalOpen = false;

  filter: any = {
    keyword: '', startDate: '', endDate: '', type: '', 
    walletId: null, minAmount: null, maxAmount: null
  };

  private filterChange$ = new Subject<void>();

  constructor(private transactionService: TransactionService, private auth: Auth) {}

  ngOnInit(): void {
    const id = this.auth.getCurrentUserId();
    if (!id) { this.auth.logout(); return; }
    this.userId = id;

    this.loadWallets();
    this.loadTransactions();
    this.loadComparison();
    this.loadNotifications();

    this.filterChange$.pipe(debounceTime(200)).subscribe(() => this.loadTransactions());
  }

  loadWallets(): void {
    this.transactionService.getWallets(this.userId).subscribe(res => this.wallets = res);
  }

  loadTransactions(): void {
    this.transactionService.getAdvancedSearch(this.userId, this.filter, this.currentPage, this.size)
      .subscribe(res => {
        this.transactions = res.content || [];
        this.totalPages = res.totalPages || 0;
      });
  }

  loadComparison(): void {
    this.transactionService.getSpendingComparison(this.userId).subscribe(res => this.comparisonData = res);
  }

  loadNotifications(): void {
    this.transactionService.getNotifications(this.userId).subscribe(res => this.notifications = res);
  }

  markAsRead(id: number): void {
    // Gọi service markAsRead nếu đã định nghĩa, hoặc logic tương tự
    this.transactionService.markNotificationAsRead(id).subscribe(() => this.loadNotifications());
  }

  onFilterChange(): void { this.currentPage = 0; this.filterChange$.next(); }

  changePage(page: number): void {
    if (page < 0 || page >= this.totalPages) return;
    this.currentPage = page;
    this.loadTransactions();
  }

  exportReport(format: 'excel' | 'pdf'): void {
    this.transactionService.downloadReportFile(this.userId, format, this.filter).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url; a.download = `Bao_Cao_${Date.now()}.${format === 'excel' ? 'xlsx' : 'pdf'}`;
      document.body.appendChild(a); a.click(); document.body.removeChild(a);
    });
  }

  closeModals(): void { this.isTransferModalOpen = false; this.isAddTransactionModalOpen = false; }

  ngOnDestroy(): void { this.filterChange$.complete(); }
}