import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CashFlowItem { label: string; income: number; expense: number; }
export interface Wallet { name: string; balance: number; }
export interface TransactionItem { title: string; amount: number; }
export interface CategoryExpense { name: string; amount: number; percentage: number; color?: string; }
export interface SpendingAlert { message: string; }

export interface DashboardData {
  totalIncome: number;
  totalExpense: number;
  netCashFlow: number;
  incomeChange: number;
  expenseChange: number;
  spendingAlerts: SpendingAlert[];
  cashFlows: CashFlowItem[];
  wallets: Wallet[];
  totalAssets: number;
  recentTransactions: TransactionItem[];
}

@Injectable({
  providedIn: 'root'
})
export class DashboardSer {
  private apiUrl = 'http://localhost:8080/api/dashboard';

  constructor(private http: HttpClient) {}

  // Lấy dữ liệu tổng quan (không bao gồm categoryExpenses nữa)
  getDashboardData(userId: number, months: number): Observable<DashboardData> {
    const params = new HttpParams()
      .set('userId', userId.toString())
      .set('months', months.toString());
    return this.http.get<DashboardData>(this.apiUrl, { params });
  }

  // API ĐỘC LẬP: Chỉ lấy dữ liệu biểu đồ tròn theo tháng và năm
  getCategoryExpenses(userId: number, month: number, year: number): Observable<CategoryExpense[]> {
    const params = new HttpParams()
      .set('userId', userId.toString())
      .set('month', month.toString())
      .set('year', year.toString());
    return this.http.get<CategoryExpense[]>(`${this.apiUrl}/expense-category`, { params });
  }
}