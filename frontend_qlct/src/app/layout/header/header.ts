import { Component, HostListener, ElementRef, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Auth } from '../../services/auth';
import { CategoryService } from '../../services/category-service';
import { TransactionService } from '../../services/transaction-service';
import { notificationService } from '../../services/notificationService';
import { ViewChild } from '@angular/core';
@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class AppHeader implements OnInit {
@ViewChild('menuContainer') menuContainer!: ElementRef;
@ViewChild('notifContainer') notifContainer!: ElementRef;

isMenuOpen = false;
isNotificationsOpen = false;
notifications: any[] = [];
isLoggedIn = false;
user: any = null;
userId = 0;

// Thuộc tính Tìm kiếm
searchQuery = '';
searchResultsCategories: any[] = [];
searchResultsTransactions: any[] = [];
showSearchResults = false;
isSearching = false;

// Cache danh mục & ví để phục vụ tìm kiếm/hiển thị tên ví
allCategories: any[] = [];
allWallets: any[] = [];
private searchDebounce: any;

constructor(
  private elementRef: ElementRef,
  private auth: Auth,
  private notificationService: notificationService,
  private categoryService: CategoryService,
  private transactionService: TransactionService,
  private router: Router,
  private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.auth.isLoggedIn$.subscribe(status => {
      this.isLoggedIn = status;
      if (status) {
        this.userId = this.auth.getCurrentUserId() ?? 0;
        if (this.userId > 0) this.loadNotifications();
      } else {
        this.notifications = [];
      }
    });

    this.auth.currentUser$.subscribe(userData => this.user = userData);
  }



  loadNotifications(): void {
    if (!this.userId) return;
    this.notificationService.getNotifications(this.userId).subscribe({
      next: (data) => this.notifications = data,
      error: (err) => console.error('Lỗi tải thông báo:', err)
    });
  }

  markAsRead(id: number, event: Event): void {
    event.stopPropagation(); // Cực kỳ quan trọng để không đóng dropdown
    this.notificationService.markAsRead(id).subscribe({
      next: () => {
        this.notifications = this.notifications.filter(n => n.id !== id);
      }
    });
  }

  toggleNotifications(event: Event): void {
    event.stopPropagation();
    this.isNotificationsOpen = !this.isNotificationsOpen;
    this.isMenuOpen = false; // Đóng menu nếu mở thông báo
  }

  toggleMenu(event: Event): void {
    event.stopPropagation();
    this.isMenuOpen = !this.isMenuOpen;
    this.isNotificationsOpen = false; // Đóng thông báo nếu mở menu
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const targetElement = event.target as HTMLElement;
    
    // Đóng dropdown tài khoản khi click ra ngoài
    const avatarContainer = this.elementRef.nativeElement.querySelector('.topbar__avatar-container');
    if (avatarContainer && targetElement && !avatarContainer.contains(targetElement)) {
      this.isMenuOpen = false;
    }

    // Đóng dropdown kết quả tìm kiếm khi click ra ngoài thanh tìm kiếm
    const searchWrapper = this.elementRef.nativeElement.querySelector('.topbar__search-wrapper');
    if (searchWrapper && targetElement && !searchWrapper.contains(targetElement)) {
      this.showSearchResults = false;
    }
  }


  loadCategoriesAndWallets() {
    // Load danh mục để tìm kiếm offline cực nhanh
    this.transactionService.getCategories().subscribe({
      next: (cats) => {
        this.allCategories = cats;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Lỗi tải danh mục ở header:', err)
    });

    // Load danh sách ví để hiển thị tên ví của giao dịch tìm thấy
    this.transactionService.getWallets().subscribe({
      next: (wallets) => {
        this.allWallets = wallets;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Lỗi tải ví ở header:', err)
    });
  }

  onSearchInput() {
    if (this.searchDebounce) {
      clearTimeout(this.searchDebounce);
    }

    const query = this.searchQuery.trim().toLowerCase();
    if (query.length < 2) {
      this.searchResultsCategories = [];
      this.searchResultsTransactions = [];
      this.showSearchResults = false;
      this.cdr.detectChanges();
      return;
    }

    this.showSearchResults = true;

    // 1. Tìm kiếm danh mục offline (local search)
    this.searchResultsCategories = this.allCategories.filter(cat => 
      cat.name?.toLowerCase().includes(query) || 
      cat.description?.toLowerCase().includes(query)
    ).slice(0, 5); // Giới hạn tối đa 5 kết quả danh mục
    this.cdr.detectChanges();

    // 2. Tìm kiếm giao dịch online (có debounce 300ms tránh spam request)
    this.isSearching = true;
    this.searchDebounce = setTimeout(() => {
      this.transactionService.getTransactions(0, 5, { query: query }).subscribe({
        next: (res) => {
          this.searchResultsTransactions = res.content || [];
          this.isSearching = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Lỗi tìm kiếm giao dịch:', err);
          this.isSearching = false;
          this.cdr.detectChanges();
        }
      });
    }, 300);
  }

  onSearchFocus() {
    if (this.searchQuery.trim().length >= 2) {
      this.showSearchResults = true;
      // Làm mới danh sách categories/wallets khi focus để dữ liệu luôn mới
      this.loadCategoriesAndWallets();
    }
  }

  onSearchBlur() {
    // Đóng sau một khoảng trễ nhỏ để sự kiện mousedown kịp thực thi chuyển hướng
    setTimeout(() => {
      this.showSearchResults = false;
    }, 200);
  }

  getWalletName(walletId: number): string {
    const wallet = this.allWallets.find(w => w.id === walletId);
    return wallet ? wallet.name : 'Ví';
  }

  goToCategory(cat: any) {
    this.showSearchResults = false;
    this.searchQuery = '';
    // Điều hướng trực tiếp đến trang sửa danh mục
    this.router.navigate(['/categories/edit', cat.id]);
  }

  goToTransaction(tx: any) {
    this.showSearchResults = false;
    this.searchQuery = '';
    // Điều hướng sang trang Giao dịch kèm theo ID giao dịch cần sửa
    this.router.navigate(['/transactions'], { queryParams: { editTxId: tx.id } });
  }

  onLogout() {
    this.auth.logout();
  }
}
