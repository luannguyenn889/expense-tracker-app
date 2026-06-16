import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { Transaction, TransferData } from '../model/transaction';
import { Auth } from './auth';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  public transactionChanges$ = new Subject<void>();

  notifyTransactionChange() {
    this.transactionChanges$.next();
  }

  private apiUrl = 'http://localhost:8080/api/transactions';
  private walletUrl = 'http://localhost:8080/api/wallets';
  private categoryUrl = 'http://localhost:8080/categories';

  constructor(
    private http: HttpClient,
    private auth: Auth
  ) {}

  // ==========================================
  // TIỆN ÍCH
  // ==========================================


  private getUserId(): number | null {
    const userInfo = this.auth['currentUser']?.value;
    if (userInfo && userInfo.id) return userInfo.id;
    const storedUser = localStorage.getItem('user_info');
    if (storedUser) {
      const user = JSON.parse(storedUser);
      return user.id;
    }
    return null;
  }

  getCurrentUserId(): number | null {
    return this.getUserId();
  }

  // ==========================================
  // CRUD & TÌM KIẾM
  // ==========================================
  
  getWallets(userId?: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.walletUrl}?userId=${userId}`);
  }

  getCategories(): Observable<any[]> {
    const userId = this.getUserId();
    let url = this.categoryUrl;
    if (userId) url += `?userId=${userId}`;
    return this.http.get<any[]>(url);
  }

  getTransactions(page: number, size: number, filter: any): Observable<any> {
    const userId = this.getUserId();
    let url = `${this.apiUrl}?userId=${userId}&page=${page}&size=${size}`;
    if (filter.startDate) url += `&startDate=${filter.startDate}`;
    if (filter.endDate) url += `&endDate=${filter.endDate}`;
    if (filter.type && ['INCOME', 'EXPENSE', 'TRANSFER'].includes(filter.type)) {
      url += `&type=${filter.type}`;
    }
    if (filter.walletId) url += `&walletId=${filter.walletId}`;
    if (filter.query) url += `&query=${encodeURIComponent(filter.query)}`;
    return this.http.get(url);
  }

  getTransactionById(id: number): Observable<any> {
    const userId = this.getUserId();
    return this.http.get<any>(`${this.apiUrl}/${id}?userId=${userId}`);
  }

  getAdvancedSearch(userId: number, filter: any, page: number, size: number): Observable<any> {
    let params = new HttpParams()
      .set('userId', userId.toString())
      .set('page', page.toString())
      .set('size', size.toString());

    if (filter.keyword) params = params.set('keyword', filter.keyword);
    if (filter.startDate) params = params.set('startDate', filter.startDate);
    if (filter.endDate) params = params.set('endDate', filter.endDate);
    if (filter.type) params = params.set('type', filter.type);
    if (filter.walletId) params = params.set('walletId', filter.walletId.toString());
    if (filter.minAmount !== null && filter.minAmount !== undefined) params = params.set('minAmount', filter.minAmount.toString());
    if (filter.maxAmount !== null && filter.maxAmount !== undefined) params = params.set('maxAmount', filter.maxAmount.toString());

    return this.http.get<any>(this.apiUrl, { params });
  }

  addTransaction(data: any): Observable<Transaction> {
    const userId = this.getUserId();
    return this.http.post<Transaction>(`${this.apiUrl}?userId=${userId}`, data);
  }

  updateTransaction(id: number, data: any): Observable<Transaction> {
    const userId = this.getUserId();
    return this.http.put<Transaction>(`${this.apiUrl}/${id}?userId=${userId}`, data);
  }

  deleteTransaction(id: number): Observable<any> {
    const userId = this.getUserId();
    return this.http.delete(`${this.apiUrl}/${id}?userId=${userId}`);
  }

  transfer(data: TransferData): Observable<any> {
    const userId = this.getUserId();
    return this.http.post(`${this.apiUrl}/transfer?userId=${userId}`, data);
  }

  // ==========================================
  // THỐNG KÊ, THÔNG BÁO & FILE
  // ==========================================
  getSpendingComparison(userId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/spending-comparison?userId=${userId}`);
  }

  downloadReportFile(userId: number, format: 'excel' | 'pdf', filter: any): Observable<Blob> {
    let params = new HttpParams().set('userId', userId.toString());
    if (filter.keyword) params = params.set('keyword', filter.keyword);
    if (filter.startDate) params = params.set('startDate', filter.startDate);
    if (filter.endDate) params = params.set('endDate', filter.endDate);
    if (filter.type) params = params.set('type', filter.type);
    if (filter.walletId) params = params.set('walletId', filter.walletId.toString());

    return this.http.get(`${this.apiUrl}/export/${format}`, { params, responseType: 'blob' });
  }

  getNotifications(userId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/notifications?userId=${userId}`);
  }

  markNotificationAsRead(id: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/notifications/${id}/read`, {});
  }
}

