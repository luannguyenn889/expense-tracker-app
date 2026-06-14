import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SavingGoalRequest {
  name: string;
  targetAmount: number;
  targetDate: string;
}

@Injectable({
  providedIn: 'root'
})
export class SavingGoalService {
  private apiUrl = 'http://localhost:8080/saving-goals';

  constructor(private http: HttpClient) {}

  addSavingGoal(goal: SavingGoalRequest, userId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/add?userId=${userId}`, goal);
  }

  getSavingGoals(userId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/list?userId=${userId}`);
  }

  contributeToGoal(request: any, userId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/contribute?userId=${userId}`, request);
  }

  updateSavingGoal(id: number, request: SavingGoalRequest): Observable<any> {
    return this.http.put(`${this.apiUrl}/edit/${id}`, request);
  }

  deleteSavingGoal(id: number, refundWalletId: number | null, userId: number): Observable<any> {
    const url = refundWalletId
      ? `${this.apiUrl}/delete/${id}?userId=${userId}&refundWalletId=${refundWalletId}`
      : `${this.apiUrl}/delete/${id}?userId=${userId}`;
    return this.http.delete(url, { responseType: 'text' });
  }
}
