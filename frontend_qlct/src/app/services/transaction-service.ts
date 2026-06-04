import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Transaction, TransferData } from '../model/transaction';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private apiUrl = 'http://localhost:8080/api/transactions';
  private walletUrl = 'http://localhost:8080/api/wallets';
  private categoryUrl = 'http://localhost:8080/categories';

  constructor(private http: HttpClient) {}

  getWallets(userId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.walletUrl}?userId=${userId}`);
  }

  getCategories(): Observable<any[]> {
    return this.http.get<any[]>(this.categoryUrl);
  }

  getTransactions(userId: number, page: number, size: number, filter: any): Observable<any> {
    let url = `${this.apiUrl}?userId=${userId}&page=${page}&size=${size}`;
    if (filter.startDate) url += `&startDate=${filter.startDate}`;
    if (filter.endDate) url += `&endDate=${filter.endDate}`;
    if (filter.type && ['INCOME', 'EXPENSE', 'TRANSFER'].includes(filter.type)) {
      url += `&type=${filter.type}`;
    }
    if (filter.walletId) url += `&walletId=${filter.walletId}`;
    return this.http.get(url);
  }

  addTransaction(userId: number, data: any): Observable<Transaction> {
    return this.http.post<Transaction>(`${this.apiUrl}?userId=${userId}`, data);
  }

  updateTransaction(id: number, userId: number, data: any): Observable<Transaction> {
    return this.http.put<Transaction>(`${this.apiUrl}/${id}?userId=${userId}`, data);
  }

  deleteTransaction(id: number, userId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}?userId=${userId}`);
  }

  transfer(userId: number, data: TransferData): Observable<any> {
    return this.http.post(`${this.apiUrl}/transfer?userId=${userId}`, data);
  }
}