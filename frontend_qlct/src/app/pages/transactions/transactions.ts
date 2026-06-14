import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ListTransaction } from '../../transactions/list-transaction/list-transaction';
import { AddTransaction } from '../../transactions/add-transaction/add-transaction';
import { EditTransaction } from '../../transactions/edit-transaction/edit-transaction';
import { Transfer } from '../../transactions/transfer/transfer';
import { TransactionService } from '../../services/transaction-service';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import {BudgetService} from '../../services/budget-service';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [CommonModule, FormsModule, ListTransaction, AddTransaction, EditTransaction, Transfer],
  templateUrl: './transactions.html',
  styleUrl: './transactions.css'
})
export class Transactions implements OnInit {

  transactions: any[] = [];
  wallets: any[] = [];
  allWallets: any[] = [];
  activeWallets: any[] = [];
  categories: any[] = [];
  filter = { startDate: '', endDate: '', type: '', walletId: null as number | null };
  currentPage = 0;
  pageSize = 10;
  totalPages = 1;

  showAddModal = false;
  showEditModal = false;
  showTransferModal = false;
  selectedTransaction: any = null;

  toastMessage: string = '';
  toastType: string = '';

  constructor(
    private transactionService: TransactionService,
    private budgetService: BudgetService,
    private cdr: ChangeDetectorRef,
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadData();

    if (sessionStorage.getItem('openAddTransaction') === 'true') {
      sessionStorage.removeItem('openAddTransaction');
      setTimeout(() => {
        this.openAddTransaction();
      }, 500);
    }

    // Lắng nghe query parameter editTxId từ chức năng tìm kiếm
    this.route.queryParams.subscribe(params => {
      const editTxId = params['editTxId'];
      if (editTxId) {
        const txId = Number(editTxId);
        // Xóa query param ngay để không bị mở lại khi reload trang
        this.router.navigate([], {
          relativeTo: this.route,
          queryParams: { editTxId: null },
          queryParamsHandling: 'merge'
        });

        this.transactionService.getTransactionById(txId).subscribe({
          next: (tx) => {
            setTimeout(() => {
              this.openEditModal(tx);
            }, 300);
          },
          error: (err) => console.error('Lỗi tải giao dịch để sửa từ tìm kiếm:', err)
        });
      }
    });


  }

  loadData() {
    this.loadWallets();
    this.transactionService.getWallets().subscribe(data => {
      this.wallets = data;
      this.cdr.detectChanges();
    });
    this.transactionService.getCategories().subscribe(data => {
      this.categories = data;
      this.cdr.detectChanges();
    });
    this.loadTransactions();
  }

  loadWallets() {
    this.transactionService.getWallets().subscribe({
      next: (data: any) => {
        this.allWallets = data;
        this.wallets = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Lỗi tải ví:', err)
    });

    this.http.get(`http://localhost:8080/api/wallets/all?userId=${this.transactionService.getCurrentUserId()}`)
      .subscribe({
        next: (data: any) => {
          this.allWallets = data;
        },
        error: (err) => console.error('Lỗi tải all ví:', err)
      });
  }

  loadTransactions() {
    this.transactionService.getTransactions(this.currentPage, this.pageSize, this.filter)
      .subscribe(data => {
        let transactions = data.content || [];
        transactions.sort((a: any, b: any) => {
          const dateA = new Date(a.transactionDate);
          const dateB = new Date(b.transactionDate);
          return dateB.getTime() - dateA.getTime();
        });
        this.transactions = transactions;
        this.totalPages = data.totalPages || 1;
        this.cdr.detectChanges();
      });
  }

  search() {
    this.currentPage = 0;
    this.loadTransactions();
  }

  changePage(page: number) {
    this.currentPage = page;
    this.loadTransactions();
  }

  clearFilter() {
    this.filter = {
      startDate: '',
      endDate: '',
      type: '',
      walletId: null
    };
    this.currentPage = 0;
    this.loadTransactions();
  }

  openAddTransaction() {
    this.showAddModal = true;
  }

  closeAddModal() {
    this.showAddModal = false;
  }

  openEditModal(transaction: any) {
    this.selectedTransaction = transaction;
    this.showEditModal = true;
  }

  closeEditModal() {
    this.showEditModal = false;
  }

  openTransferModal() {
    this.showTransferModal = true;
  }

  closeTransferModal() {
    this.showTransferModal = false;
  }

  deleteTransaction(id: number) {
    if (confirm('Xóa giao dịch này?')) {
      this.transactionService.deleteTransaction(id).subscribe({
        next: (res: any) => {
          console.log('Delete response:', res);
          this.transactions = this.transactions.filter(t => t.id !== id);
          this.cdr.detectChanges();
          alert('Xóa giao dịch thành công!');
          this.transactionService.getWallets().subscribe(data => {
            this.wallets = data;
            this.cdr.detectChanges();
          });
        },
        error: (err) => {
          console.error('Lỗi chi tiết:', err);
          if (err.status === 200 || err.status === 204) {
            this.transactions = this.transactions.filter(t => t.id !== id);
            this.cdr.detectChanges();
            alert('Xóa giao dịch thành công!');
          } else {
            alert(err.error || 'Xóa giao dịch thất bại!');
          }
        }
      });
    }
  }

  onTransactionAdded(response?: any) {
    this.closeAddModal();
    this.loadData();

    if (response && response.alertMessage) {
      this.showToast(response.alertMessage, response.alertType);
      return;
    }

    // LUẬT MỚI 1: NẾU LÀ KHOẢN THU (INCOME) -> LUÔN XANH LÁ, BỎ QUA NGÂN SÁCH
    if (response && response.type === 'INCOME') {
      this.showToast('Thêm giao dịch thành công!', 'SUCCESS');
      return;
    }

    // ĐOẠN NÀY CHỈ CHẠY KHI LÀ KHOẢN CHI (EXPENSE)
    const currentUserId = this.transactionService.getCurrentUserId() || 7;
    const today = new Date();
    const currentMonth = today.getMonth() + 1;
    const currentYear = today.getFullYear();

    this.budgetService.getBudgetProgress(currentUserId, currentMonth, currentYear).subscribe({
      next: (progressList) => {

        // LUẬT MỚI 2: Lấy chính xác CategoryId của giao dịch vừa thêm để check
        const categoryId = response ? response.categoryId : null;

        // CHỈ tìm ngân sách của đúng danh mục vừa chi
        const currentBudget = progressList.find(p => p.categoryId === categoryId);

        if (currentBudget) {
          if (currentBudget.percentage > 100) {
            this.showToast(`Cảnh báo Ngân sách: Bạn đã vượt hạn mức "${currentBudget.categoryName}"`, 'DANGER');
          } else if (currentBudget.percentage >= 80) {
            this.showToast(`Cảnh báo Ngân sách: Bạn đã tiêu gần hết "${currentBudget.categoryName}"`, 'WARNING');
          } else {
            this.showToast('Thêm giao dịch thành công!', 'SUCCESS');
          }
        } else {
          // Danh mục này chưa cài ngân sách
          this.showToast('Thêm giao dịch thành công!', 'SUCCESS');
        }
      },
      error: () => this.showToast('Thêm giao dịch thành công!', 'SUCCESS')
    });
  }

  showToast(message: string, type: string) {
    this.toastMessage = message;
    this.toastType = type;
    setTimeout(() => {
      this.toastMessage = '';
      this.cdr.detectChanges(); // ép UI cập nhật ẩn đi
    }, 5000);
  }

  onTransactionUpdated() {
    this.closeEditModal();
    this.loadData();
  }

  onTransferCompleted() {
    this.closeTransferModal();
    this.loadData();
  }
}
