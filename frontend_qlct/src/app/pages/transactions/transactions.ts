import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Subject, debounceTime } from 'rxjs';

// Import các Component con tái sử dụng từ Đoạn 2
import { ListTransaction } from '../../transactions/list-transaction/list-transaction';
import { AddTransaction } from '../../transactions/add-transaction/add-transaction';
import { EditTransaction } from '../../transactions/edit-transaction/edit-transaction';
import { Transfer } from '../../transactions/transfer/transfer';

// Import Services
import { TransactionService } from '../../services/transactionService';
import { Auth } from '../../services/auth';

@Component({
  selector: 'app-transactions',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule, 
    ListTransaction, 
    AddTransaction, 
    EditTransaction, 
    Transfer
  ],
  templateUrl: './transactions.html',
  styleUrl: './transactions.css'
})
export class Transactions implements OnInit, OnDestroy {
  // Quản lý User & Phân trang
  userId!: number;
  currentPage = 0;
  pageSize = 10;
  totalPages = 1;

  // Dữ liệu hiển thị
  transactions: any[] = [];
  wallets: any[] = [];
  allWallets: any[] = [];   
  categories: any[] = [];
  notifications: any[] = [];
  comparisonData: any = null;

  // Trạng thái đóng/mở Modal tuyển chọn từ Đoạn 2
  showAddModal = false;
  showEditModal = false;
  showTransferModal = false;
  selectedTransaction: any = null;

  // Bộ lọc nâng cao (Kế thừa đầy đủ thuộc tính từ Đoạn 1)
  filter: any = {
    keyword: '', 
    startDate: '', 
    endDate: '', 
    type: '', 
    walletId: null, 
    minAmount: null, 
    maxAmount: null
  };

  // Cơ chế Debounce chống Spam Request khi gõ từ khóa từ Đoạn 1
  private filterChange$ = new Subject<void>();

  constructor(
    private transactionService: TransactionService, 
    private auth: Auth,
    private cdr: ChangeDetectorRef,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    // Khởi tạo và kiểm tra bảo mật User ID từ Đoạn 1
    const id = this.auth.getCurrentUserId();
    if (!id) { 
      this.auth.logout(); 
      return; 
    }
    this.userId = id;

    // Tải toàn bộ tài nguyên hệ thống
    this.loadData();

    // Lắng nghe thay đổi bộ lọc, tự động kích hoạt tìm kiếm sau 200ms
    this.filterChange$.pipe(debounceTime(200)).subscribe(() => this.loadTransactions());

    // Xử lý cờ kích hoạt nhanh modal từ sessionStorage (Đoạn 2)
    if (sessionStorage.getItem('openAddTransaction') === 'true') {
      sessionStorage.removeItem('openAddTransaction');
      setTimeout(() => {
        this.openAddTransaction();
      }, 500);
    }
  }

  /**
   * Tải tổng hợp tất cả dữ liệu nền
   */
  loadData(): void {
    this.loadWallets();
    this.loadTransactions();
    this.loadComparison();
    this.loadNotifications();
    
    // Tải danh mục phân loại giao dịch
    this.transactionService.getCategories().subscribe(data => {
      this.categories = data;
      this.cdr.detectChanges();
    });
  }

  /**
   * Tải danh sách ví của người dùng
   */
  loadWallets(): void {
    // Luồng 1: Qua service hệ thống
    this.transactionService.getWallets(this.userId).subscribe({
      next: (data: any) => {
        this.allWallets = data;        
        this.wallets = data;          
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Lỗi tải ví:', err)
    });
    
    // Luồng 2: Gọi trực tiếp Endpoint dự phòng theo Đoạn 2
    this.http.get(`http://localhost:8080/api/wallets/all?userId=${this.userId}`)
      .subscribe({
        next: (data: any) => {
          this.allWallets = data; 
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Lỗi tải all ví:', err)
      });
  }

  /**
   * Tải danh sách giao dịch dựa trên bộ lọc nâng cao và phân trang
   */
  loadTransactions(): void {
    // Sử dụng hàm tìm kiếm nâng cao (getAdvancedSearch hoặc getTransactions tùy tên định nghĩa ở Service của bạn)
    this.transactionService.getAdvancedSearch(this.userId, this.filter, this.currentPage, this.pageSize)
      .subscribe({
        next: (res: any) => {
          let content = res.content || [];
          
          // Sắp xếp giao dịch mới nhất lên đầu (Đoạn 2)
          content.sort((a: any, b: any) => {
            const dateA = new Date(a.transactionDate);
            const dateB = new Date(b.transactionDate);
            return dateB.getTime() - dateA.getTime();
          });

          this.transactions = content;
          this.totalPages = res.totalPages || 1;
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Lỗi tải danh sách giao dịch:', err)
      });
  }

  /**
   * Tải dữ liệu biến động chi tiêu tháng hiện tại
   */
  loadComparison(): void {
    this.transactionService.getSpendingComparison(this.userId).subscribe(res => {
      this.comparisonData = res;
      this.cdr.detectChanges();
    });
  }

  /**
   * Tải danh sách thông báo nhắc nhở tài chính
   */
  loadNotifications(): void {
    this.transactionService.getNotifications(this.userId).subscribe(res => {
      this.notifications = res || [];
      this.cdr.detectChanges();
    });
  }

  /**
   * Đánh dấu thông báo đã đọc
   */
  markAsRead(id: number): void {
    this.transactionService.markNotificationAsRead(id).subscribe(() => {
      this.loadNotifications();
    });
  }

  /**
   * Kích hoạt khi người dùng thay đổi dữ liệu trên bộ lọc đầu vào
   */
  onFilterChange(): void { 
    this.currentPage = 0; 
    this.filterChange$.next(); 
  }

  /**
   * Thực hiện tìm kiếm thủ công (Dành cho nút bấm)
   */
  search(): void { 
    this.currentPage = 0; 
    this.loadTransactions(); 
  }

  /**
   * Chuyển đổi trang dữ liệu
   */
  changePage(page: number): void {
    if (page < 0 || page >= this.totalPages) return;
    this.currentPage = page;
    this.loadTransactions();
  }

  /**
   * Reset toàn bộ dữ liệu lọc về trạng thái ban đầu
   */
  clearFilter(): void {
    this.filter = {
      keyword: '',
      startDate: '',
      endDate: '',
      type: '',
      walletId: null,
      minAmount: null,
      maxAmount: null
    };
    this.currentPage = 0;
    this.loadTransactions();
  }

  /**
   * Xuất file báo cáo thống kê định dạng Excel hoặc PDF
   */
  exportReport(format: 'excel' | 'pdf'): void {
    this.transactionService.downloadReportFile(this.userId, format, this.filter).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url; 
      a.download = `Bao_Cao_${Date.now()}.${format === 'excel' ? 'xlsx' : 'pdf'}`;
      document.body.appendChild(a); 
      a.click(); 
      document.body.removeChild(a);
    });
  }

  /**
   * Xóa một giao dịch theo ID kèm cơ chế bắt lỗi HTTP linh hoạt
   */
  deleteTransaction(id: number): void {
    if (confirm('Xóa giao dịch này?')) {
      this.transactionService.deleteTransaction(id).subscribe({
        next: (res: any) => {
          this.transactions = this.transactions.filter(t => t.id !== id);
          alert('Xóa giao dịch thành công!');
          this.loadWallets(); // Cập nhật lại số dư ví sau khi xóa dữ liệu
        },
        error: (err) => {
          console.error('Lỗi chi tiết khi xóa:', err);
          // Xử lý trường hợp Backend trả về chuỗi text thuần gây lỗi Parse JSON (Status 200/204 thành công giả)
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

  // --- QUẢN LÝ ĐÓNG MỞ MODAL DIỄN HOẠT (Tương thích với HTML đã dung hợp) ---
  openAddTransaction(): void { this.showAddModal = true; }
  closeAddModal(): void { this.showAddModal = false; }

  openEditModal(transaction: any): void {
    this.selectedTransaction = transaction;
    this.showEditModal = true;
  }
  closeEditModal(): void { this.showEditModal = false; }

  openTransferModal(): void { this.showTransferModal = true; }
  closeTransferModal(): void { this.showTransferModal = false; }

  // Hỗ trợ hàm đóng tất cả nhanh từ Đoạn 1
  closeModals(): void {
    this.showAddModal = false;
    this.showEditModal = false;
    this.showTransferModal = false;
  }

  // Phản hồi sự kiện khi thao tác thành công trên các Component con
  onTransactionAdded(): void { this.closeAddModal(); this.loadData(); }
  onTransactionUpdated(): void { this.closeEditModal(); this.loadData(); }
  onTransferCompleted(): void { this.closeTransferModal(); this.loadData(); }

  ngOnDestroy(): void { 
    // Hủy Stream RxJS để tránh rò rỉ bộ nhớ (Memory Leak)
    this.filterChange$.complete(); 
  }
}