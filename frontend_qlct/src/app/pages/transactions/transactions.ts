import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Subject, debounceTime } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';

import { ListTransaction } from '../../transactions/list-transaction/list-transaction';
import { AddTransaction } from '../../transactions/add-transaction/add-transaction';
import { EditTransaction } from '../../transactions/edit-transaction/edit-transaction';
import { Transfer } from '../../transactions/transfer/transfer';
import { TransactionService } from '../../services/transaction-service';
import { BudgetService } from '../../services/budget-service';
import { Auth } from '../../services/auth';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule, FormsModule, ListTransaction, AddTransaction, EditTransaction, Transfer],
  templateUrl: './transactions.html',
  styleUrl: './transactions.css'
})
export class Transactions implements OnInit, OnDestroy {
  userId!: number;
  transactions: any[] = [];
  wallets: any[] = [];
  allWallets: any[] = [];
  categories: any[] = [];
  comparisonData: any = null;
  currentPage = 0;
  pageSize = 10;
  totalPages = 1;

  showAddModal = false;
  showEditModal = false;
  showTransferModal = false;
  selectedTransaction: any = null;

  toastMessage: string = '';
  toastType: string = '';

  filter: any = { keyword: '', startDate: '', endDate: '', type: '', walletId: null, minAmount: null, maxAmount: null };
  private filterChange$ = new Subject<void>();

  constructor(
    private transactionService: TransactionService,
    private budgetService: BudgetService,
    private auth: Auth,
    private cdr: ChangeDetectorRef,
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const id = this.auth.getCurrentUserId();
    if (!id) { this.auth.logout(); return; }
    this.userId = id;

    this.loadData();
    this.filterChange$.pipe(debounceTime(200)).subscribe(() => this.loadTransactions());

    if (sessionStorage.getItem('openAddTransaction') === 'true') {
      sessionStorage.removeItem('openAddTransaction');
      setTimeout(() => this.openAddTransaction(), 500);
    }

    this.route.queryParams.subscribe(params => {
      if (params['editTxId']) {
        this.transactionService.getTransactionById(Number(params['editTxId'])).subscribe(tx => {
          setTimeout(() => this.openEditModal(tx), 300);
        });
      }
    });
  }

  loadData(): void {
    this.loadWallets();
    this.loadTransactions();
    this.loadComparison();
    this.transactionService.getCategories().subscribe(data => { this.categories = data; this.cdr.detectChanges(); });
  }

  loadWallets(): void {
    this.transactionService.getWallets(this.userId).subscribe(data => {

      this.allWallets = data;
      this.wallets = data;
      this.cdr.detectChanges();
    });
  }

loadTransactions(): void {
  this.transactionService.getAdvancedSearch(
    this.userId,
    this.filter,
    this.currentPage,
    this.pageSize
  ).subscribe({
    next: (res: any) => {

      console.log("Response:", res);

      this.transactions = (res.content || []).sort(
        (a: any, b: any) =>
          new Date(b.transactionDate).getTime() -
          new Date(a.transactionDate).getTime()
      );

      console.log("Transactions:", this.transactions);

      this.totalPages = res.totalPages || 1;

      this.cdr.detectChanges();
    },
    error: err => {
      console.log(err);
    }
  });
}

  loadComparison(): void {
    this.transactionService.getSpendingComparison(this.userId).subscribe(res => {
      this.comparisonData = res;
      this.cdr.detectChanges();
    });
  }

  onFilterChange(): void { this.currentPage = 0; this.filterChange$.next(); }
  search(): void { this.currentPage = 0; this.loadTransactions(); }
  changePage(page: number): void { if (page >= 0 && page < this.totalPages) { this.currentPage = page; this.loadTransactions(); } }
  clearFilter(): void { this.filter = { keyword: '', startDate: '', endDate: '', type: '', walletId: null, minAmount: null, maxAmount: null }; this.loadTransactions(); }

  exportReport(format: 'excel' | 'pdf'): void {
    this.transactionService.downloadReportFile(this.userId, format, this.filter).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url; a.download = `Bao_Cao_${Date.now()}.${format === 'excel' ? 'xlsx' : 'pdf'}`;
      a.click();
    });
  }

  deleteTransaction(id: number): void {
    if (confirm('Xóa giao dịch này?')) {
      this.transactionService.deleteTransaction(id).subscribe({
        next: () => { this.transactions = this.transactions.filter(t => t.id !== id); this.loadWallets(); alert('Xóa thành công!'); },
        error: (err) => { if (err.status === 200) { this.transactions = this.transactions.filter(t => t.id !== id); alert('Xóa thành công!'); } }
      });
    }
  }

  // Quản lý Modal
  openAddTransaction() { this.showAddModal = true; }
  closeAddModal() { this.showAddModal = false; }
  openEditModal(transaction: any) { this.selectedTransaction = transaction; this.showEditModal = true; }
  closeEditModal() { this.showEditModal = false; }
  openTransferModal() { this.showTransferModal = true; }
  closeTransferModal() { this.showTransferModal = false; }

  // Xử lý sau khi thêm/sửa/xóa
onTransactionAdded(response?: any) {
  this.closeAddModal();

  // 1. Nếu có thông báo alert từ server, ưu tiên hiện luôn và reload
  if (response && response.alertMessage) {
    this.showToast(response.alertMessage, response.alertType);
    this.loadData();
    return;
  }

  // 2. Xử lý INCOME riêng (không cần check ngân sách)
  if (response && response.type === 'INCOME') {
    this.showToast('Thêm giao dịch thành công!', 'SUCCESS');
    this.loadData();
    return;
  }

  // 3. Xử lý EXPENSE với kiểm tra ngân sách
  const currentUserId = this.transactionService.getCurrentUserId() || 7;
  const today = new Date();
  
  this.budgetService.getBudgetProgress(currentUserId, today.getMonth() + 1, today.getFullYear()).subscribe({
    next: (progressList) => {
      const categoryId = response ? response.categoryId : null;
      const currentBudget = progressList.find(p => p.categoryId === categoryId);

      if (currentBudget) {
        if (currentBudget.percentage > 100) {
          this.showToast(`Cảnh báo: Đã vượt hạn mức "${currentBudget.categoryName}"`, 'DANGER');
        } else if (currentBudget.percentage >= 80) {
          this.showToast(`Cảnh báo: Gần hết hạn mức "${currentBudget.categoryName}"`, 'WARNING');
        } else {
          this.showToast('Thêm giao dịch thành công!', 'SUCCESS');
        }
      } else {
        this.showToast('Thêm giao dịch thành công!', 'SUCCESS');
      }
      
      // Load dữ liệu sau khi đã xử lý xong logic ngân sách để UI cập nhật mới nhất
      this.loadData();
    },
    error: () => {
      this.showToast('Thêm giao dịch thành công!', 'SUCCESS');
      this.loadData();
    }
  });
}

  showToast(message: string, type: string) {
    this.toastMessage = message; this.toastType = type;
    setTimeout(() => { this.toastMessage = ''; this.cdr.detectChanges(); }, 5000);
  }

  onTransactionUpdated(): void { this.closeEditModal(); this.loadData(); }
  onTransferCompleted(): void { this.closeTransferModal(); this.loadData(); }

  ngOnDestroy(): void { this.filterChange$.complete(); }
}