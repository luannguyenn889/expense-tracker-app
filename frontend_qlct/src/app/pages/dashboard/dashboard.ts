import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Auth } from '../../services/auth';
import { CategoryService } from '../../services/category-service';
import { Category } from '../../model/category';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {

  aiAdvice: string = '';
  isAiThinking: boolean = false;
  username: string = '';
  userId: number | null = null;

  // Danh mục gần đây của user (tối đa 7)
  recentCategories: Category[] = [];
  isCategoryLoading: boolean = true;

  constructor(
    private http: HttpClient,
    private auth: Auth,
    private categoryService: CategoryService
  ) {}

  ngOnInit() {
    this.auth.currentUser$.subscribe({
      next: (user) => {
        if (user && user.username) {
          this.username = user.username;
        } else {
          this.username = '';
        }
        if (user && user.id) {
          this.userId = user.id;
          this.loadRecentCategories(user.id);
        }
      }
    });
  }

  /** Lấy tối đa 7 danh mục của người dùng đang đăng nhập */
  loadRecentCategories(userId: number) {
    this.isCategoryLoading = true;
    this.categoryService.getAllCategories(userId).subscribe({
      next: (categories) => {
        // Lọc riêng danh mục của user (userId != null), lấy tối đa 7
        this.recentCategories = categories
          .filter(c => c.userId != null)
          .slice(0, 7);
        this.isCategoryLoading = false;
      },
      error: (err) => {
        console.error('Lỗi tải danh mục:', err);
        this.isCategoryLoading = false;
      }
    });
  }

  /** Trả về màu nền cho icon dựa trên color của category, hoặc màu mặc định */
  getCategoryBgColor(category: Category): string {
    return category.color ? category.color + '22' : 'var(--color-primary-container)';
  }

  /** Trả về màu icon dựa trên color của category */
  getCategoryIconColor(category: Category): string {
    return category.color ?? 'var(--color-primary)';
  }

  askAiForAdvice() {
    this.isAiThinking = true;
    this.aiAdvice = '';

    const url = `http://localhost:8080/api/ai/advice?username=${this.username}`;
    this.http.get(url).subscribe({
      next: (res: any) => {
        this.aiAdvice = res.message;
        this.isAiThinking = false;
      },
      error: (err) => {
        console.error('Lỗi AI:', err);
        this.aiAdvice = 'Xin lỗi, trợ lý AI hiện đang đi vắng. Hãy thử lại sau nhé.';
        this.isAiThinking = false;
      }
    });
  }
}
