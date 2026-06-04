import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CashFlow } from '../model/cashFlow';

@Injectable({
  providedIn: 'root'
})
export class CashFlowService {
private apiUrl = 'http://localhost:8080/api/dashboard/cashFlow';

  constructor(private http: HttpClient) {}

  getCashFlow(): Observable<CashFlow[]> {
    return this.http.get<CashFlow[]>(this.apiUrl);
  }
}