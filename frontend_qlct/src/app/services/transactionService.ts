import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TransactionService {
  private apiUrl = 'http://localhost:8080/api/transactions';
  private walletUrl = 'http://localhost:8080/api/wallets';

  constructor(private http: HttpClient) {}

  getWallets(userId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.walletUrl}?userId=${userId}`);
  }
  getSpendingComparison(userId: number): Observable<any> {
  // Trả về object dạng { currentMonthTotal: number, lastMonthTotal: number, percentageChange: number }
  return this.http.get<any>(`${this.apiUrl}/spending-comparison?userId=${userId}`);
  }
  // Gửi thông tin tìm kiếm động map chính xác với cấu trúc CSDL Backend
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
markNotificationAsRead(id: number): Observable<any> {
  // Thay đổi URL theo đúng API bạn định nghĩa ở Backend
  return this.http.put(`${this.apiUrl}/notifications/${id}/read`, {});
}
  downloadReportFile(userId: number, format: 'excel' | 'pdf', filter: any): Observable<Blob> {
    let params = new HttpParams().set('userId', userId.toString());
    
    if (filter.keyword) params = params.set('keyword', filter.keyword);
    if (filter.startDate) params = params.set('startDate', filter.startDate);
    if (filter.endDate) params = params.set('endDate', filter.endDate);
    if (filter.type) params = params.set('type', filter.type);
    if (filter.walletId) params = params.set('walletId', filter.walletId.toString());
    if (filter.minAmount !== null) params = params.set('minAmount', filter.minAmount.toString());
    if (filter.maxAmount !== null) params = params.set('maxAmount', filter.maxAmount.toString());

    return this.http.get(`${this.apiUrl}/export/${format}`, {
      params,
      responseType: 'blob' // Ép kiểu dữ liệu tải file stream nhị phân nhả về từ API
    });
  }

  getNotifications(userId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/notifications?userId=${userId}`);
  }
}