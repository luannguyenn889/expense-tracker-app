import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Category} from '../model/category';

@Injectable({
  providedIn: 'root',
})
export class CategoryService {

  private apiUrl = 'http://localhost:8080/categories';

  constructor(private http: HttpClient) {}

  getAllCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(this.apiUrl);
  }

  getCategoryById(id: string): Observable<Category> {
    return this.http.get<Category>(`${this.apiUrl}/${id}`);
  }

  addCategory(category: Category): Observable<Category> {
    return this.http.post<Category>(`${this.apiUrl}/add`, category);
  }
  
  updateCategory(id: string, category: Category): Observable<Category> {
    return this.http.put<Category>(`${this.apiUrl}/update/${id}`, category);
  }

  deleteCategory(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/delete/${id}`, { responseType: 'text' });
  }
  // private apiUrl = 'http://localhost:8080/api'; // URL Spring Boot
  //
  // constructor(private http: HttpClient) {}
  //
  // // GET /api/products
  // getProducts(): Observable<Product[]> {
  //   return this.http.get<Product[]>(`${this.apiUrl}/products`).pipe(
  //     retry(1),
  //     catchError(this.handleError)
  //   );
  // }
  //
  // // GET /api/products/{id}
  // getProductById(id: number): Observable<Product> {
  //   return this.http.get<Product>(`${this.apiUrl}/products/${id}`).pipe(
  //     catchError(this.handleError)
  //   );
  // }
  //
  // private handleError(error: HttpErrorResponse) {
  //   let errorMessage = 'Đã xảy ra lỗi không xác định';
  //   if (error.error instanceof ErrorEvent) {
  //     errorMessage = `Lỗi phía client: ${error.error.message}`;
  //   } else {
  //     errorMessage = `Lỗi server: ${error.status} - ${error.message}`;
  //   }
  //   return throwError(() => new Error(errorMessage));
  // }
}
