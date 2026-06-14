import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface BudgetRequest {
  categoryId: string;
  amount: number;
  month: number;
  year: number;
}

@Injectable({
  providedIn: 'root'
})
export class BudgetService {
  private apiUrl = 'http://localhost:8080/budgets';

  constructor(private http: HttpClient) {}

  addBudget(budget: BudgetRequest, userId: number): Observable<any> {
    // Truyền userId qua params để khớp với Backend API
    return this.http.post(`${this.apiUrl}/add?userId=${userId}`, budget);
  }

  getBudgetProgress(userId: number, month: number, year: number): Observable<BudgetProgress[]> {
    return this.http.get<BudgetProgress[]>(`${this.apiUrl}/progress?userId=${userId}&month=${month}&year=${year}`);
  }
}

// Thêm interface này vào file budget-service.ts
export interface BudgetProgress {
  categoryId: string;
  categoryName: string;
  budgetAmount: number;
  actualSpend: number;
  percentage: number;
}



