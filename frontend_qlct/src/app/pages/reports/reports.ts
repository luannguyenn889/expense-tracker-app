import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { Auth } from '../../services/auth';
import { DashboardSer, CashFlowItem, Wallet, TransactionItem, CategoryExpense, SpendingAlert } from '../../services/dashboardSer';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule, HttpClient } from '@angular/common/http'; // Đã thêm HttpClient
import { CategoryService } from '../../services/category-service';
import { Category } from '../../model/category';
import { TransactionService } from '../../services/transaction-service';
import { WalletService } from '../../services/wallet-service';
import {
  Chart,
  ArcElement,
  Tooltip,
  Legend,
  DoughnutController,
  Title,
} from 'chart.js';
import { Wallet as WalletModel } from '../../model/wallet';

Chart.register(ArcElement, Tooltip, Legend, DoughnutController, Title);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './reports.html',
  styleUrls: ['./reports.css']
})
export class Reports implements OnInit {

  selectedPeriod: number = 6;
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
  categoryExpenses: CategoryExpense[] = [];
  
  userId!: number;
  username: string = ''; // Thêm biến username
  pieChart: Chart | null = null;
  isAiThinking: boolean = false; // Thêm biến AI
  aiAdvice: string = ''; 
  isLoading: boolean = true; // Thêm biến Loading
  filter: any = {
    keyword: '',
    startDate: '',
    endDate: '',
    type: '',
    walletId: null
  };
  transactions: any[] = [];
  constructor(
    private dashboardSer: DashboardSer,
    private auth: Auth,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private walletService: WalletService,
    private categoryService: CategoryService,
    private transactionService: TransactionService,
    private http: HttpClient // Thêm HttpClient
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
        this.username = userObj.username || '';
        this.loadGeneralData();
        this.loadPieChartData();
        this.loadWallets(); // Load ví ngay khi khởi tạo
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
      }
    });

    this.transactionService.transactionChanges$.subscribe(() => {
      if (this.userId) {
        this.loadWallets();
      }
    });
  }

  loadPieChartData(): void {
    if (!this.userId) return;

    this.dashboardSer.getCategoryExpenses(this.userId, this.selectedMonth, this.selectedYear).subscribe({
      next: (data) => {
        this.categoryExpenses = data || [];
        this.renderPieChart(); // Gọi hàm render biểu đồ từ dev
        this.cdr.detectChanges();
      }
    });
  }

  // --- MỚI: Hàm render biểu đồ từ dev ---
  renderPieChart(): void {
    const canvas = document.getElementById('pieChart') as HTMLCanvasElement;
    if (!canvas) return;

    if (this.pieChart) {
      this.pieChart.destroy();
      this.pieChart = null;
    }

    const defaultPalette = ['#EF5350', '#EC407A', '#AB47BC', '#5C6BC0', '#42A5F5', '#26C6DA', '#66BB6A', '#FFCA28'];
    
    this.pieChart = new Chart(canvas, {
      type: 'doughnut',
      data: {
        labels: this.categoryExpenses.map(c => c.name || 'Chưa đặt tên'),
        datasets: [{
          data: this.categoryExpenses.map(c => Math.abs(c.amount)),
          backgroundColor: defaultPalette,
        }]
      },
      options: { responsive: true, maintainAspectRatio: false }
    });
  }

  // --- MỚI: Hàm AI Advice từ dev ---
  askAiForAdvice() {
    this.isAiThinking = true;
    this.aiAdvice = '';
    const url = `http://localhost:8080/api/ai/advice?username=${this.username}`;
    this.http.get(url).subscribe({
      next: (res: any) => {
        this.aiAdvice = res.message;
        this.isAiThinking = false;
        this.cdr.detectChanges(); // Hiển thị kết quả lời khuyên ngay lập tức
      },
      error: () => {
        this.aiAdvice = 'Xin lỗi, trợ lý AI hiện đang đi vắng.';
        this.isAiThinking = false;
        this.cdr.detectChanges(); // Hiển thị thông báo lỗi ngay lập tức
      }
    });
  }

  // --- MỚI: Hàm Load ví từ dev ---
  loadWallets() {
    this.isLoading = true;
    this.walletService.getWallets().subscribe({
      next: (data) => {
        this.wallets = data || [];
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  getTotalBalance(): number {
    return this.wallets.reduce((sum, w) => sum + (w.balance || 0), 0);
  }

  // Giữ nguyên các hàm cũ của bạn:
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
}
