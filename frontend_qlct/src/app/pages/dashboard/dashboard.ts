import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Auth } from '../../services/auth';
import { CategoryService } from '../../services/category-service';
import { Category } from '../../model/category';
import {
  Chart,
  ArcElement,
  Tooltip,
  Legend,
  DoughnutController,
  Title,
} from 'chart.js';

// Đăng ký các thành phần Chart.js cần dùng
Chart.register(ArcElement, Tooltip, Legend, DoughnutController, Title);

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit, OnDestroy {

  aiAdvice: string = '';
  isAiThinking: boolean = false;
  username: string = '';
  userId: number | null = null;

  // Danh mục gần đây của user (tối đa 7)
  recentCategories: Category[] = [];
  isCategoryLoading: boolean = true;

  // Biểu đồ
  private pieChart: Chart | null = null;
  hasExpenseCategories: boolean = false;
  private allUserCategories: Category[] = [];

  constructor(
    private http: HttpClient,
    private auth: Auth,
    private categoryService: CategoryService,
    private cdr: ChangeDetectorRef
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

  ngOnDestroy() {
    if (this.pieChart) {
      this.pieChart.destroy();
      this.pieChart = null;
    }
  }

  /** Lấy danh mục của user, hiển thị bảng + biểu đồ */
  loadRecentCategories(userId: number) {
    this.isCategoryLoading = true;
    this.categoryService.getAllCategories(userId).subscribe({
      next: (categories) => {
        const userCats = categories.filter(c => c.userId != null);

        // 1. Cập nhật dữ liệu bảng (tối đa 7)
        this.recentCategories = userCats.slice(0, 7);

        // 2. Xác định có danh mục Chi tiêu không
        const expenseCats = userCats.filter(c => c.type === 'EXPENSE');
        this.hasExpenseCategories = expenseCats.length > 0;
        this.allUserCategories = userCats;

        // 3. Tắt loading → Angular render @if block (canvas xuất hiện trong DOM)
        this.isCategoryLoading = false;

        // 4. Force Angular detect changes để canvas được render TRƯỚC khi vẽ
        this.cdr.detectChanges();

        // 5. Sau khi DOM đã cập nhật → vẽ chart
        if (this.hasExpenseCategories) {
          this.buildPieChart(expenseCats);
        }
      },
      error: (err) => {
        console.error('Lỗi tải danh mục:', err);
        this.isCategoryLoading = false;
      }
    });
  }

  /** Vẽ biểu đồ tròn phân bổ chi tiêu theo danh mục EXPENSE */
  buildPieChart(expenseCategories: Category[]) {
    const canvas = document.getElementById('categoryPieCanvas') as HTMLCanvasElement;
    if (!canvas) {
      console.warn('Canvas không tìm thấy, thử lại sau 200ms...');
      setTimeout(() => this.buildPieChart(expenseCategories), 200);
      return;
    }

    // Huỷ chart cũ nếu tồn tại
    if (this.pieChart) {
      this.pieChart.destroy();
      this.pieChart = null;
    }

    // Palette màu mặc định dùng khi category không có color
    const defaultPalette = [
      '#EF5350', '#EC407A', '#AB47BC', '#5C6BC0',
      '#42A5F5', '#26C6DA', '#26A69A', '#66BB6A',
      '#D4E157', '#FFCA28', '#FFA726', '#FF7043',
      '#8D6E63', '#78909C'
    ];

    const labels     = expenseCategories.map(c => c.name);
    const dataValues = expenseCategories.map(c => c.monthlyBudget ?? 1);
    const bgColors   = expenseCategories.map((c, i) =>
      c.color ?? defaultPalette[i % defaultPalette.length]
    );
    const borderColors = bgColors.map(c => c + 'bb');

    this.pieChart = new Chart(canvas, {
      type: 'doughnut',
      data: {
        labels,
        datasets: [{
          data: dataValues,
          backgroundColor: bgColors,
          borderColor: borderColors,
          borderWidth: 2,
          hoverOffset: 10,
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '58%',
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              padding: 14,
              font: { size: 12 },
              usePointStyle: true,
              pointStyleWidth: 10,
              color: '#555',
            }
          },
          tooltip: {
            callbacks: {
              label: (ctx) => {
                const cat = expenseCategories[ctx.dataIndex];
                if (cat.monthlyBudget) {
                  return ` ${cat.name}: ${cat.monthlyBudget.toLocaleString('vi-VN')} ₫/tháng`;
                }
                return ` ${cat.name}`;
              }
            }
          }
        }
      }
    });
  }

  /** Màu nền icon */
  getCategoryBgColor(category: Category): string {
    return category.color ? category.color + '22' : 'var(--color-primary-container)';
  }

  /** Màu icon */
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
