// import { Injectable } from '@angular/core';
// import { HttpClient, HttpParams } from '@angular/common/http';
// import { Observable } from 'rxjs';
// import { Transaction, TransferData } from '../model/transaction';
// import { Auth } from './auth';

// @Injectable({
//   providedIn: 'root'
// })
// export class TransactionService {
//   private apiUrl = 'http://localhost:8080/api/transactions';
//   private walletUrl = 'http://localhost:8080/api/wallets';
//   private categoryUrl = 'http://localhost:8080/categories';

//   constructor(
//     private http: HttpClient,
//     private auth: Auth
//   ) {}

//   /**
//    * Hàm nội bộ tự động lấy UserId từ Auth Service hoặc LocalStorage
//    */
//   private getUserId(): number | null {
//     const userInfo = this.auth['currentUser']?.value;
//     if (userInfo && userInfo.id) {
//       return userInfo.id;
//     }
//     const storedUser = localStorage.getItem('user_info');
//     if (storedUser) {
//       const user = JSON.parse(storedUser);
//       return user.id;
//     }
//     return null;
//   }

//   /**
//    * Expose hàm lấy UserId ra ngoài nếu Component cần dùng
//    */
//   getCurrentUserId(): number | null {
//     return this.getUserId();
//   }

//   // ==========================================
//   // VÍ & DANH MỤC (WALLETS & CATEGORIES)
//   // ==========================================

//   /**
//    * Lấy danh sách ví (Hỗ trợ cả việc tự lấy userId hoặc truyền vào nếu có)
//    */
//   getWallets(userId?: number): Observable<any[]> {
//     const id = userId || this.getUserId();
//     return this.http.get<any[]>(`${this.walletUrl}?userId=${id}`);
//   }

//   /**
//    * Lấy danh sách danh mục phân loại theo User
//    */
//   getCategories(): Observable<any[]> {
//     const userId = this.getUserId();
//     let url = this.categoryUrl;
//     if (userId) {
//       url += `?userId=${userId}`;
//     }
//     return this.http.get<any[]>(url);
//   }

//   // ==========================================
//   // TRUY VẤN GIAO DỊCH (TRANSACTIONS SEARCH)
//   // ==========================================

//   /**
//    * Lấy danh sách giao dịch phân trang cơ bản (Đoạn 1)
//    */
//   getTransactions(page: number, size: number, filter: any): Observable<any> {
//     const userId = this.getUserId();
//     let url = `${this.apiUrl}?userId=${userId}&page=${page}&size=${size}`;
//     if (filter.startDate) url += `&startDate=${filter.startDate}`;
//     if (filter.endDate) url += `&endDate=${filter.endDate}`;
//     if (filter.type && ['INCOME', 'EXPENSE', 'TRANSFER'].includes(filter.type)) {
//       url += `&type=${filter.type}`;
//     }
//     if (filter.walletId) url += `&walletId=${filter.walletId}`;
//     return this.http.get(url);
//   }

//   /**
//    * Tìm kiếm nâng cao bằng HttpParams động map chuẩn cấu trúc Backend (Đoạn 2)
//    */
// getAdvancedSearch(
//   userId: number,
//   filter: any,
//   page: number,
//   size: number
// ): Observable<any> {

//   let params = new HttpParams()
//     .set('userId', userId.toString())
//     .set('page', page.toString())
//     .set('size', size.toString());

//   if (filter.keyword)
//     params = params.set('keyword', filter.keyword);

//   if (filter.startDate)
//     params = params.set('startDate', filter.startDate);

//   if (filter.endDate)
//     params = params.set('endDate', filter.endDate);

//   if (filter.type)
//     params = params.set('type', filter.type);

//   if (filter.walletId)
//     params = params.set('walletId', filter.walletId.toString());

//   if (filter.minAmount !== null && filter.minAmount !== undefined)
//     params = params.set('minAmount', filter.minAmount.toString());

//   if (filter.maxAmount !== null && filter.maxAmount !== undefined)
//     params = params.set('maxAmount', filter.maxAmount.toString());

//   // DEBUG
//   console.log('API URL:', this.apiUrl);
//   console.log('PARAMS:', params.toString());

//   return this.http.get<any>(
//     this.apiUrl,
//     { params }
//   );
// }
//   // ==========================================
//   // THAO TÁC CRUD GIAO DỊCH (CUD & TRANSFER)
//   // ==========================================

//   addTransaction(data: any): Observable<Transaction> {
//     const userId = this.getUserId();
//     return this.http.post<Transaction>(`${this.apiUrl}?userId=${userId}`, data);
//   }

//   updateTransaction(id: number, data: any): Observable<Transaction> {
//     const userId = this.getUserId();
//     return this.http.put<Transaction>(`${this.apiUrl}/${id}?userId=${userId}`, data);
//   }

//   deleteTransaction(id: number): Observable<any> {
//     const userId = this.getUserId();
//     return this.http.delete(`${this.apiUrl}/${id}?userId=${userId}`);
//   }

//   transfer(data: TransferData): Observable<any> {
//     const userId = this.getUserId();
//     return this.http.post(`${this.apiUrl}/transfer?userId=${userId}`, data);
//   }

//   // ==========================================
//   // TÍNH NĂNG MỞ RỘNG (THỐNG KÊ, THÔNG BÁO & FILE)
//   // ==========================================

//   /**
//    * Thống kê biến động chi tiêu tháng này so với tháng trước
//    */
//   getSpendingComparison(userId: number): Observable<any> {
//     return this.http.get<any>(`${this.apiUrl}/spending-comparison?userId=${userId}`);
//   }

//   /**
//    * Tải file báo cáo thống kê dạng nhị phân (Excel / PDF)
//    */
//   downloadReportFile(userId: number, format: 'excel' | 'pdf', filter: any): Observable<Blob> {
//     let params = new HttpParams().set('userId', userId.toString());
    
//     if (filter.keyword) params = params.set('keyword', filter.keyword);
//     if (filter.startDate) params = params.set('startDate', filter.startDate);
//     if (filter.endDate) params = params.set('endDate', filter.endDate);
//     if (filter.type) params = params.set('type', filter.type);
//     if (filter.walletId) params = params.set('walletId', filter.walletId.toString());
    
//     if (filter.minAmount !== null && filter.minAmount !== undefined) {
//       params = params.set('minAmount', filter.minAmount.toString());
//     }
//     if (filter.maxAmount !== null && filter.maxAmount !== undefined) {
//       params = params.set('maxAmount', filter.maxAmount.toString());
//     }

//     return this.http.get(`${this.apiUrl}/export/${format}`, {
//       params,
//       responseType: 'blob' // Định dạng nhận luồng file Stream từ Backend
//     });
//   }

//   /**
//    * Lấy danh sách thông báo của người dùng
//    */
//   getNotifications(userId: number): Observable<any> {
//     return this.http.get(`${this.apiUrl}/notifications?userId=${userId}`);
//   }

//   /**
//    * Đánh dấu thông báo là đã đọc
//    */
//   markNotificationAsRead(id: number): Observable<any> {
//     return this.http.put(`${this.apiUrl}/notifications/${id}/read`, {});
//   }
// }