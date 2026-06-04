import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Wallet } from '../model/wallet';

@Injectable({ providedIn: 'root' })
export class WalletService {
  private apiUrl = 'http://localhost:8080/api/wallets';

  constructor(private http: HttpClient) {}

  getWallets(userId: number): Observable<Wallet[]> {
    return this.http.get<Wallet[]>(`${this.apiUrl}?userId=${userId}`);
  }

  addWallet(userId: number, data: any): Observable<Wallet> {
    return this.http.post<Wallet>(`${this.apiUrl}?userId=${userId}`, data);
  }

  updateWallet(id: number, userId: number, data: any): Observable<Wallet> {
    return this.http.put<Wallet>(`${this.apiUrl}/${id}?userId=${userId}`, data);
  }

  deleteWallet(id: number, userId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}?userId=${userId}`);
  }

  hasTransactions(id: number, userId: number): Observable<{ hasTransactions: boolean }> {
    return this.http.get<{ hasTransactions: boolean }>(`${this.apiUrl}/${id}/has-transactions?userId=${userId}`);
  }
}