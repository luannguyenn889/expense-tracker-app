import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class notificationService {

  private apiUrl =
    'http://localhost:8080/api/transactions/notifications';

  constructor(private http: HttpClient) {}

  getNotifications(userId: number): Observable<any[]> {
    return this.http.get<any[]>(
      `${this.apiUrl}?userId=${userId}`
    );
  }

  markAsRead(notificationId: number): Observable<any> {
    return this.http.post(
      `${this.apiUrl}/read/${notificationId}`,
      {}
    );
  }
}