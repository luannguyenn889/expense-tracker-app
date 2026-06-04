import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { Auth } from '../../services/auth';
import { DashboardSer, CashFlowItem, Wallet, TransactionItem, CategoryExpense, SpendingAlert } from '../../services/dashboardSer';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css']
})
export class Dashboard implements OnInit {

  selectedPeriod: number = 6; // Bộ lọc chung cho Dashboard

  // Các biến lọc độc lập cho khu vực Biểu đồ tròn
  selectedMonth: number = new Date().getMonth() + 1; 
  selectedYear: number = new Date().getFullYear();    

  monthsList: number[] = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];
  yearsList: number[] = [];

  totalIncome: number = 0;
  totalExpense: number = 0;
  netCashFlow: number = 0;
  incomeChange: number = 0;
  expenseChange: number = 0;
  
  spendingAlerts: SpendingAlert[] = [];
  cashFlows: CashFlowItem[] = [];
  wallets: Wallet[] = [];
  totalAssets: number = 0;
  recentTransactions: TransactionItem[] = [];
  
  // Dữ liệu hiển thị biểu đồ tròn và cột % bên cạnh
  categoryExpenses: CategoryExpense[] = [];

  userId!: number;

  constructor(
    private dashboardSer: DashboardSer,
    private auth: Auth,
    private router: Router,
    private cdr: ChangeDetectorRef 
  ) {
    const currentYear = new Date().getFullYear();
    for (let y = currentYear - 3; y <= currentYear + 1; y++) {
      this.yearsList.push(y);
    }
  }

  ngOnInit(): void {
    this.initializeDashboard();
  }

  private initializeDashboard(): void {
    let attempts = 0;
    const maxAttempts = 10;

    const checkUserAndLoad = () => {
      const userObj = this.auth.getCurrentUser();
      if (userObj && userObj.id) {
        this.userId = Number(userObj.id);
        this.loadGeneralData();  // Tải dữ liệu tổng quan
        this.loadPieChartData(); // Tải riêng dữ liệu biểu đồ tròn
      } else {
        attempts++;
        if (attempts < maxAttempts) {
          setTimeout(checkUserAndLoad, 100);
        } else {
          this.router.navigate(['/login']);
        }
      }
    };
    setTimeout(checkUserAndLoad, 50);
  }

  // Hàm chạy khi thay đổi bộ lọc chu kỳ tổng (Tháng này, 3 tháng, 6 tháng...)
  loadGeneralData(): void {
    if (!this.userId) return;

    this.dashboardSer.getDashboardData(this.userId, this.selectedPeriod).subscribe({
      next: (data) => {
        if (data) {
          this.totalIncome = data.totalIncome || 0;
          this.totalExpense = data.totalExpense || 0;
          this.netCashFlow = data.netCashFlow || 0;
          this.incomeChange = data.incomeChange || 0;
          this.expenseChange = data.expenseChange || 0;
          this.spendingAlerts = data.spendingAlerts || [];
          this.cashFlows = data.cashFlows || [];
          this.wallets = data.wallets || [];
          this.totalAssets = data.totalAssets || 0;
          this.recentTransactions = data.recentTransactions || [];

          this.cdr.detectChanges(); 
        }
      },
      error: (err) => console.error('Lỗi tải dữ liệu tổng quan:', err)
    });
  }

  // Hàm chạy RIÊNG khi thay đổi tháng hoặc năm ở biểu đồ tròn
  loadPieChartData(): void {
    if (!this.userId) return;

    this.dashboardSer.getCategoryExpenses(this.userId, this.selectedMonth, this.selectedYear).subscribe({
      next: (data) => {
        this.categoryExpenses = data || [];
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Lỗi tải dữ liệu biểu đồ tròn:', err)
    });
  }

  getBarHeight(amount: number): number {
    if (amount <= 0 || !this.cashFlows || this.cashFlows.length === 0) return 0;
    const maxHeight = 140; 
    const maxInList = Math.max(...this.cashFlows.map(item => Math.max(item.income, item.expense)));
    const maxAmount = maxInList > 0 ? maxInList : 100000;
    return (amount / maxAmount) * maxHeight;
  }

  getSavingRate(): number {
    if (!this.totalIncome || this.totalIncome <= 0) return 0;
    return (this.netCashFlow / this.totalIncome) * 100;
  }

  getPieStrokeArray(index: number): string {
    if (!this.categoryExpenses || !this.categoryExpenses[index]) return '0 100';
    const currentPercent = this.categoryExpenses[index].percentage || 0;
    return `${currentPercent} ${100 - currentPercent}`;
  }

  getPieStrokeOffset(index: number): number {
    let accumulatedPercent = 0;
    for (let i = 0; i < index; i++) {
      accumulatedPercent += this.categoryExpenses[i].percentage || 0;
    }
    return 100 - accumulatedPercent + 25; 
  }
}