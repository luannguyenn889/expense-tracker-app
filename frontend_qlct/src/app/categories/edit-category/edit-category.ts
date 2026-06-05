import {Component, OnInit, ChangeDetectorRef} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {Router, ActivatedRoute, RouterLink} from '@angular/router';
import {CategoryService} from '../../services/category-service';
import {Category} from '../../model/category';
import {CommonModule} from '@angular/common';
import {Auth} from '../../services/auth'; // Import Auth service để lấy thông tin người dùng đang đăng nhập

@Component({
  selector: 'app-edit-category',
  imports: [FormsModule, RouterLink, CommonModule],
  templateUrl: './edit-category.html',
  styleUrl: './edit-category.css',
})
export class EditCategory implements OnInit {
  category: Category = new Category('', '', '', 'EXPENSE');
  isLoading: boolean = true;
  categoryId: string = '';
  userId: number | null = null; // Lưu trữ ID người dùng đăng nhập

  constructor(
    private categoryService: CategoryService,
    private router: Router,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef,
    private auth: Auth // Inject Auth service
  ) {}

  ngOnInit(): void {
    this.categoryId = this.route.snapshot.paramMap.get('id') || '';

    // Lắng nghe thông tin người dùng đăng nhập
    this.auth.currentUser$.subscribe({
      next: (user) => {
        if (user && user.id) {
          this.userId = user.id;
        }
      },
      error: (err) => {
        console.error('Error fetching user info in EditCategory:', err);
      }
    });

    if (this.categoryId) {
      this.categoryService.getCategoryById(this.categoryId).subscribe({
        next: (data) => {
          this.category = data;
          this.isLoading = false;
          this.cdr.detectChanges(); // Ép Angular cập nhật giao diện ngay lập tức
        },
        error: (err) => {
          console.error('Error fetching category:', err);
          this.isLoading = false;
          this.cdr.detectChanges(); // Ép cập nhật giao diện khi xảy ra lỗi
        }
      });
    }
  }

  onSubmit(): void {
    // Đảm bảo người dùng đã đăng nhập trước khi thực hiện
    if (!this.userId) {
      alert('Không tìm thấy thông tin đăng nhập. Vui lòng đăng nhập lại.');
      this.router.navigate(['/login']);
      return;
    }

    if (!this.category.name || !this.category.icon) {
      alert('Vui lòng điền đầy đủ các trường bắt buộc');
      return;
    }

    this.category.userId = this.userId;

    this.categoryService.updateCategory(this.categoryId, this.category).subscribe({
      next: () => {
        this.router.navigate(['/list-category']);
      },
      error: (err) => {
        console.error('Error updating category:', err);
        alert('Cập nhật danh mục thất bại');
      }
    });
  }
}
