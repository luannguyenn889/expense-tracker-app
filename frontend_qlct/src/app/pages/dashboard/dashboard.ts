import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Auth } from '../../services/auth';
import { DashboardSer, CashFlowItem, CategoryExpense } from '../../services/dashboardSer';
import { CategoryService } from '../../services/category-service';
import { Category } from '../../model/category';
import { TransactionService } from '../../services/transaction-service';
import { WalletService } from '../../services/wallet-service';
import { Wallet } from '../../model/wallet';
import {
  Chart,
  ArcElement,
  Tooltip,
  Legend,
  DoughnutController,
  Title,
} from 'chart.js';

import { Wallets } from '../wallets/wallets'; 

// Đăng ký các thành phần Chart.js cần dùng
Chart.register(ArcElement, Tooltip, Legend, DoughnutController, Title);
 
@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit, OnDestroy {
 
  // ===== User =====
  userId: number | null = null;
  username: string = '';
 
  // ===== Bộ lọc =====
  selectedPeriod: number = 6;
  selectedMonth: number = new Date().getMonth() + 1;
  selectedYear: number = new Date().getFullYear();
  monthsList: number[] = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];
  yearsList: number[] = [];
 
  // ===== Thẻ tổng quan (Thu / Chi / Số dư) =====
  totalIncome: number = 0;
  totalExpense: number = 0;
  netCashFlow: number = 0;
 
  // ===== Biểu đồ thu chi theo tháng =====
  cashFlows: CashFlowItem[] = [];
 
  // ===== Ví & số dư hiện tại =====
  wallets: Wallet[] = [];
  isLoading: boolean = true;
 
  // ===== Biểu đồ tròn chi tiêu theo danh mục =====
  categoryExpenses: CategoryExpense[] = [];
  hasExpenseCategories: boolean = false;
  private pieChart: Chart | null = null;
 
  // ===== Bảng "Hoạt động gần đây" (danh mục của user) =====
  recentCategories: Category[] = [];
  isCategoryLoading: boolean = true;
 
  // ===== Trợ lý Tài chính AI =====
  isAiThinking: boolean = false;
  aiAdvice: string = '';
 
  constructor(
    private dashboardSer: DashboardSer,
    private auth: Auth,
    private cdr: ChangeDetectorRef,
    private walletService: WalletService,
    private categoryService: CategoryService,
    private transactionService: TransactionService,
    private http: HttpClient
  ) {
    const currentYear = new Date().getFullYear();
    for (let y = currentYear - 3; y <= currentYear + 1; y++) {
      this.yearsList.push(y);
    }
  }
 
  ngOnInit(): void {
    this.auth.currentUser$.subscribe({
      next: (user) => {
        this.username = user?.username || '';
        if (user && user.id) {
          this.userId = Number(user.id);
          this.loadGeneralData();
          this.loadPieChartData();
          this.loadRecentCategories();
          this.loadWallets();
        }
      }
    });
 
    // Tự động làm mới dữ liệu mỗi khi có giao dịch mới được thêm/sửa/xoá
    this.transactionService.transactionChanges$.subscribe(() => {
      if (this.userId) {
        this.loadGeneralData();
        this.loadPieChartData();
        this.loadRecentCategories();
        this.loadWallets();
      }
    });
 
    if (sessionStorage.getItem('needRefreshWallets')) {
      sessionStorage.removeItem('needRefreshWallets');
      this.loadWallets();
    }
  }
 
  ngOnDestroy(): void {
    if (this.pieChart) {
      this.pieChart.destroy();
      this.pieChart = null;
    }
  }
 
  // ===== Tổng thu / tổng chi / dòng tiền ròng theo khoảng thời gian =====
  loadGeneralData(): void {
    if (!this.userId) return;
 
    this.dashboardSer.getDashboardData(this.userId, this.selectedPeriod).subscribe({
      next: (data) => {
        if (!data) return;
        this.totalIncome = data.totalIncome || 0;
        this.totalExpense = data.totalExpense || 0;
        this.netCashFlow = data.netCashFlow || 0;
        this.cashFlows = data.cashFlows || [];
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Lỗi tải dữ liệu tổng quan:', err)
    });
  }
 
  /** Chiều cao cột (px) tỉ lệ theo giá trị lớn nhất trong danh sách cashFlows */
  getBarHeight(amount: number): number {
    if (!amount || amount <= 0 || !this.cashFlows || this.cashFlows.length === 0) return 0;
    const maxHeight = 140;
    const maxInList = Math.max(...this.cashFlows.map(item => Math.max(item.income, item.expense)));
    const maxAmount = maxInList > 0 ? maxInList : 100000;
    return (amount / maxAmount) * maxHeight;
  }
 
  /** Tỷ lệ tiết kiệm = dòng tiền ròng / tổng thu nhập */
  getSavingRate(): number {
    if (!this.totalIncome || this.totalIncome <= 0) return 0;
    return (this.netCashFlow / this.totalIncome) * 100;
  }
 
  // ===== Biểu đồ tròn: chi tiêu theo danh mục (lọc theo tháng/năm) =====
  loadPieChartData(): void {
    if (!this.userId) return;
    this.isCategoryLoading = true;
 
    this.dashboardSer.getCategoryExpenses(this.userId, this.selectedMonth, this.selectedYear).subscribe({
      next: (data) => {
        this.categoryExpenses = data || [];
        this.hasExpenseCategories = this.categoryExpenses.length > 0;
        this.isCategoryLoading = false;
        this.cdr.detectChanges();
 
        if (this.hasExpenseCategories) {
          this.buildPieChart();
        }
      },
      error: (err) => {
        console.error('Lỗi tải dữ liệu chi tiêu theo danh mục:', err);
        this.hasExpenseCategories = false;
        this.isCategoryLoading = false;
      }
    });
  }
 
  /** Vẽ biểu đồ tròn (doughnut) lên canvas#categoryPieCanvas */
  private buildPieChart(): void {
  // 1. Kiểm tra dữ liệu: Nếu mảng rỗng, không làm gì cả
  if (!this.categoryExpenses || this.categoryExpenses.length === 0) {
    console.warn("Dữ liệu biểu đồ trống.");
    return;
  }

  const canvas = document.getElementById('categoryPieCanvas') as HTMLCanvasElement;
  if (!canvas) {
    // Nếu chưa thấy canvas, thử lại sau 200ms
    setTimeout(() => this.buildPieChart(), 200);
    return;
  }

  // 2. Hủy chart cũ an toàn
  if (this.pieChart) {
    this.pieChart.destroy();
    this.pieChart = null;
  }

  const defaultPalette = [
    '#EF5350', '#EC407A', '#AB47BC', '#5C6BC0',
    '#42A5F5', '#26C6DA', '#26A69A', '#66BB6A',
    '#D4E157', '#FFCA28', '#FFA726', '#FF7043'
  ];

  // 3. Map dữ liệu an toàn (Dùng "||" để tránh undefined)
  const labels = this.categoryExpenses.map(c => c.name || 'Chưa đặt tên');
  const values = this.categoryExpenses.map(c => Math.abs(c.amount || 0));
  const bgColors = this.categoryExpenses.map((_, i) => defaultPalette[i % defaultPalette.length]);

  // 4. Vẽ biểu đồ
  this.pieChart = new Chart(canvas, {
    type: 'doughnut',
    data: {
      labels: labels,
      datasets: [{
        data: values,
        backgroundColor: bgColors,
        borderColor: bgColors.map(c => c + 'bb'),
        borderWidth: 2,
        hoverOffset: 10,
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '58%',
      plugins: {
        legend: {
          position: 'bottom',
          labels: {
            padding: 14,
            font: { size: 12 },
            usePointStyle: true,
            color: '#555',
          }
        },
        tooltip: {
          callbacks: {
            label: (ctx) => {
              const cat = this.categoryExpenses[ctx.dataIndex];
              const name = cat.name || 'Chưa đặt tên';
              const amount = (cat.amount || 0).toLocaleString('vi-VN');
              const percent = cat.percentage != null ? ` (${cat.percentage.toFixed(1)}%)` : '';
              return ` ${name}: ${amount} ₫${percent}`;
            }
          }
        }
      }
    }
  });
}
 
  // ===== Bảng "Hoạt động gần đây": danh mục của user (tối đa 7) =====
  loadRecentCategories(): void {
    if (!this.userId) return;
 
    this.categoryService.getAllCategories(this.userId).subscribe({
      next: (categories) => {
        const userCats = categories.filter(c => c.userId != null);
        this.recentCategories = userCats.slice(0, 7);
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Lỗi tải danh mục:', err)
    });
  }
 
  getCategoryBgColor(category: Category): string {
    return (category.color && category.color.trim() !== '') ? category.color + '22' : 'var(--color-primary-container)';
  }
 
  getCategoryIconColor(category: Category): string {
    return (category.color && category.color.trim() !== '') ? category.color : 'var(--color-primary)';
  }
 
  // ===== Ví & số dư hiện tại =====
  loadWallets(): void {
    this.isLoading = true;
    this.walletService.getWallets().subscribe({
      next: (data) => {
        this.wallets = data || [];
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Lỗi tải ví:', err);
        this.wallets = [];
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }
 
  getTotalBalance(): number {
    return this.wallets.reduce((sum, w) => sum + (w.balance || 0), 0);
  }
 
  // ===== Trợ lý Tài chính AI =====
  askAiForAdvice(): void {
    this.isAiThinking = true;
    this.aiAdvice = '';
    this.cdr.detectChanges(); // Cập nhật ngay để disable nút, tránh spam click

    const url = `http://localhost:8080/api/ai/advice?username=${this.username}`;
    this.http.get(url).subscribe({
      next: (res: any) => {
        this.aiAdvice = res.message;
        this.isAiThinking = false;
        this.cdr.detectChanges(); // Hiển thị kết quả lời khuyên ngay lập tức
      },
      error: (err) => {
        console.error('Lỗi AI:', err);
        this.aiAdvice = 'Xin lỗi, trợ lý AI hiện đang đi vắng. Hãy thử lại sau nhé.';
        this.isAiThinking = false;
        this.cdr.detectChanges();
      }
    });
  }
}
