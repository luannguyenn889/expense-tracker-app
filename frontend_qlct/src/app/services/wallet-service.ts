import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class WalletService {
  private apiUrl = 'http://localhost:8080/api/wallets';

  constructor(private http: HttpClient) {}

  private getUserId(): number | null {
    const storedUser = localStorage.getItem('user_info');
    if (storedUser) {
      const user = JSON.parse(storedUser);
      return user.id;
    }
    return null;
  }

  getWallets(): Observable<any[]> {
    const userId = this.getUserId();
    return this.http.get<any[]>(`${this.apiUrl}?userId=${userId}`);
  }

  getAllWallets(): Observable<any[]> {
    const userId = this.getUserId();
    return this.http.get<any[]>(`${this.apiUrl}/all?userId=${userId}`);
  }

  getTotalBalance(): Observable<number> {
    const userId = this.getUserId();
    return this.http.get<number>(`${this.apiUrl}/total-balance?userId=${userId}`);
  }

  addWallet(walletData: any): Observable<any> {
    const userId = this.getUserId();
    return this.http.post(`${this.apiUrl}?userId=${userId}`, walletData);
  }

  updateWallet(id: number, walletData: any): Observable<any> {
    const userId = this.getUserId();
    return this.http.put(`${this.apiUrl}/${id}?userId=${userId}`, walletData);
  }

  deleteWallet(id: number): Observable<any> {
    const userId = this.getUserId();
    return this.http.delete(`${this.apiUrl}/${id}?userId=${userId}`);
  }

  hasTransactions(id: number): Observable<any> {
    const userId = this.getUserId();
    return this.http.get(`${this.apiUrl}/${id}/has-transactions?userId=${userId}`);
  }
}