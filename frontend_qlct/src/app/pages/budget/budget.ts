import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategoryService } from '../../services/category-service';
import { BudgetService, BudgetRequest, BudgetProgress } from '../../services/budget-service';
import { Category } from '../../model/category';
import { Auth } from '../../services/auth';

@Component({
  selector: 'app-budget',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="transactions-container">
      <div class="header-actions">
        <div>
          <h2>{{ editingBudgetId ? 'Cập nhật hạn mức' : 'Thiết lập hạn mức' }}</h2>
          <p class="page-subtitle">Quản lý và giới hạn chi tiêu hàng tháng của bạn.</p>
        </div>
      </div>

      <div class="filters form-wrapper">
        <div *ngIf="errorMessage" class="alert alert-error w-100">{{ errorMessage }}</div>
        <div *ngIf="successMessage" class="alert alert-success w-100">{{ successMessage }}</div>

        <form (ngSubmit)="onSubmit()" #budgetForm="ngForm" class="w-100">
          <div class="filter-row">
            <div class="filter-group">
              <label for="category">Danh mục chi tiêu</label>
              <select id="category" name="category" [(ngModel)]="selectedCategoryId" required [disabled]="editingBudgetId !== null">
                <option value="" disabled selected>-- Chọn danh mục --</option>
                <option *ngFor="let cat of expenseCategories" [value]="cat.id">
                  {{ cat.name }}
                </option>
              </select>
            </div>

            <div class="filter-group">
              <label for="monthYear">Tháng áp dụng</label>
              <input type="month" id="monthYear" name="monthYear"
                     [(ngModel)]="selectedMonthYear"
                     (ngModelChange)="onMonthChange()" required [disabled]="editingBudgetId !== null" />
            </div>

            <div class="filter-group">
              <label for="amount">Số tiền giới hạn (VNĐ)</label>
              <input type="number" id="amount" name="amount" [(ngModel)]="amount" required min="1000" placeholder="Ví dụ: 2000000" />
            </div>
          </div>

          <div class="filter-actions justify-end mt-4">
            <button type="button" *ngIf="editingBudgetId" (click)="cancelEdit()" class="btn-cancel">
              Hủy
            </button>
            <button type="submit" [disabled]="!budgetForm.valid || isLoading" class="btn-search">
              <span class="material-symbols-outlined" *ngIf="!isLoading">{{ editingBudgetId ? 'save' : 'add' }}</span>
              <span *ngIf="!isLoading">{{ editingBudgetId ? 'Cập Nhật' : 'Lưu Hạn Mức' }}</span>
              <span *ngIf="isLoading">Đang xử lý...</span>
            </button>
          </div>
        </form>
      </div>

      <div class="progress-section mt-4">
        <h3 class="section-title">Theo dõi tiến độ ({{ selectedMonthYear }})</h3>

        <div class="progress-list">
          <div *ngIf="budgetProgresses.length === 0" class="empty-state">
            Chưa có hạn mức nào được thiết lập trong tháng này.
          </div>

          <div class="progress-card" *ngFor="let p of budgetProgresses">
            <div class="progress-header">
              <div>
                <span class="cat-name">{{ p.categoryName }}</span>
                <span class="cat-amount ml-2">{{ p.actualSpend | number }} / {{ p.budgetAmount | number }} đ</span>
              </div>
              <div class="action-buttons">
                <span class="material-symbols-outlined icon-btn edit" title="Sửa hạn mức" (click)="editBudget(p)">edit</span>
                <span class="material-symbols-outlined icon-btn delete" title="Xóa hạn mức" (click)="deleteBudget(p.budgetId)">delete</span>
              </div>
            </div>
            <div class="progress-bar-bg">
              <div class="progress-bar-fill"
                   [style.width.%]="p.percentage > 100 ? 100 : p.percentage"
                   [ngClass]="{
                     'bg-success': p.percentage < 50,
                     'bg-warning': p.percentage >= 50 && p.percentage <= 80,
                     'bg-danger': p.percentage > 80
                   }">
              </div>
            </div>
            <div class="progress-footer">
              <span class="percentage-text" [ngClass]="{'text-danger': p.percentage > 100}">
                {{ p.percentage }}%
              </span>
              <span *ngIf="p.percentage > 100" class="text-danger fw-bold">Vượt hạn mức!</span>
            </div>
          </div>
        </div>
      </div>

    </div>
  `,
  styles: [`
    .transactions-container { padding: 24px 32px; max-width: 1400px; margin: 0 auto; font-family: 'Work Sans', sans-serif; }
    .header-actions { display: flex; justify-content: space-between; align-items: center; margin-bottom: 28px; }
    .header-actions h2 { margin: 0; font-size: 24px; font-weight: 600; color: #1a1c1c; }
    .page-subtitle { font-size: 14px; color: #64748b; margin: 4px 0 0 0; }

    .filters.form-wrapper { display: flex; flex-direction: column; gap: 1rem; background: rgba(255, 255, 255, 0.85); border: 1px solid rgba(226, 232, 240, 0.8); border-radius: 20px; padding: 2rem; }
    .filter-row { display: flex; gap: 1rem; flex-wrap: wrap; }
    .filter-group { display: flex; flex-direction: column; gap: 0.5rem; flex: 1; min-width: 200px; }
    .filter-group label { font-size: 0.75rem; font-weight: 700; text-transform: uppercase; color: #64748b; }
    .filter-group input, .filter-group select { width: 100%; padding: 0.6rem 1rem; border: 1px solid #e2e8f0; border-radius: 12px; font-size: 14px; background: white; box-sizing: border-box; }
    .filter-group input:focus, .filter-group select:focus { outline: none; border-color: #006493; }
    .filter-group input:disabled, .filter-group select:disabled { background: #f1f5f9; cursor: not-allowed; }

    .filter-actions { display: flex; gap: 8px; align-items: flex-end; }
    .justify-end { justify-content: flex-end; }
    .mt-4 { margin-top: 1.5rem; }
    .w-100 { width: 100%; }

    .btn-search { background: linear-gradient(135deg, #006493 0%, #004b70 100%); color: white; border: none; border-radius: 12px; padding: 0.6rem 1.5rem; cursor: pointer; font-weight: 600; height: 42px; display: inline-flex; align-items: center; gap: 6px; transition: all 0.2s ease; }
    .btn-search:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 6px 12px rgba(0, 100, 147, 0.3); }
    .btn-search:disabled { background: #94a3b8; cursor: not-allowed; }

    .btn-cancel { background: transparent; color: #64748b; border: 1px solid #cbd5e1; border-radius: 12px; padding: 0.6rem 1.5rem; cursor: pointer; font-weight: 600; height: 42px; transition: all 0.2s ease; }
    .btn-cancel:hover { background: #f1f5f9; color: #334155; }

    .alert { padding: 12px 16px; border-radius: 12px; margin-bottom: 20px; font-size: 14px; box-sizing: border-box; }
    .alert-error { background-color: #fef2f2; color: #b91c1c; border: 1px solid #fecaca; }
    .alert-success { background-color: #f0fdf4; color: #15803d; border: 1px solid #bbf7d0; }

    .section-title { font-size: 18px; font-weight: 600; color: #1a1c1c; margin-bottom: 16px; margin-top: 32px; padding-left: 8px; border-left: 4px solid #006493;}
    .progress-list { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; }
    .progress-card { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 16px; padding: 16px; box-shadow: 0 2px 4px rgba(0,0,0,0.02); }

    .progress-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; font-size: 14px; }
    .cat-name { font-weight: 600; color: #334155; }
    .cat-amount { color: #64748b; }
    .ml-2 { margin-left: 8px; }

    .action-buttons { display: flex; gap: 8px; }
    .icon-btn { font-size: 18px; cursor: pointer; color: #cbd5e1; transition: color 0.2s ease; }
    .icon-btn.edit:hover { color: #006493; }
    .icon-btn.delete:hover { color: #ef4444; }

    .progress-bar-bg { width: 100%; height: 8px; background-color: #f1f5f9; border-radius: 99px; overflow: hidden; margin-bottom: 8px; }
    .progress-bar-fill { height: 100%; border-radius: 99px; transition: width 0.4s ease; }

    .bg-success { background-color: #10b981; }
    .bg-warning { background-color: #f59e0b; }
    .bg-danger { background-color: #ef4444; }

    .progress-footer { display: flex; justify-content: space-between; font-size: 12px; color: #64748b; }
    .text-danger { color: #ef4444 !important; }
    .fw-bold { font-weight: 700; }
    .empty-state { grid-column: 1 / -1; text-align: center; padding: 32px; color: #94a3b8; font-style: italic; background: #f8fafc; border-radius: 12px; border: 1px dashed #cbd5e1; }

    @media (max-width: 768px) {
      .transactions-container { padding: 16px; }
      .filters.form-wrapper { padding: 1rem; }
      .filter-group { min-width: 100%; }
      .btn-search, .btn-cancel { width: 100%; justify-content: center; }
      .progress-list { grid-template-columns: 1fr; }
    }
  `]
})
export class Budget implements OnInit {
  expenseCategories: Category[] = [];
  userId: number = 0;

  selectedCategoryId: string = '';
  amount: number | null = null;
  selectedMonthYear: string = '';

  editingBudgetId: number | null = null; // Cờ theo dõi trạng thái Sửa

  isLoading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';

  budgetProgresses: BudgetProgress[] = [];

  constructor(
    private categoryService: CategoryService,
    private budgetService: BudgetService,
    private cdr: ChangeDetectorRef,
    private auth: Auth
  ) {}

  ngOnInit(): void {
    this.setDefaultMonth();
    this.auth.currentUser$.subscribe({
      next: (user) => {
        if (user && user.id) {
          this.userId = user.id;
        } else {
          const userInfoStr = localStorage.getItem('user_info');
          if (userInfoStr) {
            const userInfo = JSON.parse(userInfoStr);
            this.userId = userInfo && userInfo.id ? userInfo.id : 7;
          } else {
            this.userId = 7;
          }
        }
        this.fetchCategories();
        this.fetchBudgetProgress();
      },
      error: (err) => {
        console.error('Error fetching user info:', err);
        this.userId = 7;
        this.fetchCategories();
        this.fetchBudgetProgress();
      }
    });
  }

  setDefaultMonth(): void {
    const today = new Date();
    const year = today.getFullYear();
    const month = (today.getMonth() + 1).toString().padStart(2, '0');
    this.selectedMonthYear = `${year}-${month}`;
  }

  onMonthChange(): void {
    if (this.selectedMonthYear) {
      this.cancelEdit(); // Hủy trạng thái sửa nếu đổi tháng
      this.fetchBudgetProgress();
    }
  }

  fetchCategories(): void {
    this.categoryService.getAllCategories(this.userId).subscribe({
      next: (data) => {
        this.expenseCategories = data.filter(cat => {
          if (cat.type !== 'EXPENSE') return false;
          return true;
        });

        const uniqueCategories = [];
        const seenNames = new Set();
        for (let i = this.expenseCategories.length - 1; i >= 0; i--) {
          const currentCat = this.expenseCategories[i];
          if (!seenNames.has(currentCat.name)) {
            seenNames.add(currentCat.name);
            uniqueCategories.unshift(currentCat);
          }
        }
        this.expenseCategories = uniqueCategories;
      },
      error: (err) => console.error('Fetch Categories Error:', err)
    });
  }

  fetchBudgetProgress(): void {
    if (!this.selectedMonthYear) return;
    const [yearStr, monthStr] = this.selectedMonthYear.split('-');

    this.budgetService.getBudgetProgress(this.userId, parseInt(monthStr, 10), parseInt(yearStr, 10))
      .subscribe({
        next: (data) => {
          this.budgetProgresses = data;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Fetch Progress Error:', err);
        }
      });
  }

  // Khởi động chế độ Sửa
  editBudget(p: BudgetProgress): void {
    this.editingBudgetId = p.budgetId;
    this.selectedCategoryId = p.categoryId;
    this.amount = p.budgetAmount;

    // Cuộn mượt mà lên trên form
    window.scrollTo({ top: 0, behavior: 'smooth' });
    this.errorMessage = '';
    this.successMessage = '';
  }

  // Hủy chế độ Sửa
  cancelEdit(): void {
    this.editingBudgetId = null;
    this.selectedCategoryId = '';
    this.amount = null;
    this.errorMessage = '';
    this.successMessage = '';
  }

  // Hàm xóa hạn mức
  deleteBudget(budgetId: number): void {
    if (confirm('Bạn có chắc chắn muốn xóa hạn mức này không? Dữ liệu chi tiêu sẽ không bị ảnh hưởng.')) {
      this.budgetService.deleteBudget(budgetId, this.userId).subscribe({
        next: () => {
          this.successMessage = 'Đã xóa hạn mức!';
          this.fetchBudgetProgress();
          if (this.editingBudgetId === budgetId) this.cancelEdit();
        },
        error: (err) => {
          this.errorMessage = err.error || 'Xóa thất bại!';
          this.cdr.detectChanges();
        }
      });
    }
  }

  onSubmit(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.selectedCategoryId || !this.amount || !this.selectedMonthYear) {
      this.errorMessage = 'Vui lòng điền đầy đủ thông tin.';
      return;
    }

    const [yearStr, monthStr] = this.selectedMonthYear.split('-');
    const request: BudgetRequest = {
      categoryId: this.selectedCategoryId,
      amount: this.amount,
      month: parseInt(monthStr, 10),
      year: parseInt(yearStr, 10)
    };

    this.isLoading = true;

    // PHÂN NHÁNH: CẬP NHẬT HOẶC THÊM MỚI
    if (this.editingBudgetId) {
      this.budgetService.updateBudget(this.editingBudgetId, request, this.userId).subscribe({
        next: (res) => {
          this.isLoading = false;
          this.successMessage = 'Cập nhật hạn mức thành công!';
          this.cancelEdit();
          this.fetchBudgetProgress();
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = err.error || 'Đã xảy ra lỗi khi cập nhật.';
          this.cdr.detectChanges();
        }
      });
    } else {
      this.budgetService.addBudget(request, this.userId).subscribe({
        next: (res) => {
          this.isLoading = false;
          this.successMessage = 'Thiết lập hạn mức thành công!';
          this.selectedCategoryId = '';
          this.amount = null;
          this.fetchBudgetProgress();
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = err.error || 'Đã xảy ra lỗi khi lưu hạn mức.';
          this.cdr.detectChanges();
        }
      });
    }
  }
}
