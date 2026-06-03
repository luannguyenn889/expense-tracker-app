import {Component, OnInit} from '@angular/core'; // Thêm OnInit
import {FormsModule} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {CategoryService} from '../../services/category-service';
import {Category} from '../../model/category';
import { CommonModule } from '@angular/common';
import {Auth} from '../../services/auth'; // Import Auth service để lấy thông tin người dùng đang đăng nhập

@Component({
  selector: 'app-add-category',
  imports: [FormsModule, RouterLink, CommonModule],
  templateUrl: './add-category.html',
  styleUrl: './add-category.css',
})
export class AddCategory implements OnInit {
  category: Category = new Category('', '', '', 'EXPENSE');
  userId: number | null = null; // Lưu trữ ID người dùng đăng nhập

  constructor(
    private categoryService: CategoryService,
    private router: Router,
    private auth: Auth // Inject Auth service
  ) {}

  ngOnInit(): void {
    // Lấy thông tin người dùng đang đăng nhập để gán cho category
    this.auth.currentUser$.subscribe({
      next: (user) => {
        if (user && user.id) {
          this.userId = user.id;
          this.category.userId = user.id; // Gán userId trực tiếp cho object category
        }
      },
      error: (err) => {
        console.error('Error fetching user info in AddCategory:', err);
      }
    });
  }

  onSubmit(): void {
    // Đảm bảo người dùng đã đăng nhập trước khi thực hiện
    if (!this.userId) {
      alert('Không tìm thấy thông tin đăng nhập. Vui lòng đăng nhập lại.');
      this.router.navigate(['/login']);
      return;
    }

    // Basic validation
    if (!this.category.name || !this.category.icon) {
      alert('Vui lòng điền đầy đủ các trường bắt buộc');
      return;
    }

    this.category.userId = this.userId;

    this.categoryService.addCategory(this.category).subscribe({
      next: () => {
        this.router.navigate(['/list-category']);
      },
      error: (err) => {
        console.error('Error adding category:', err);
        alert('Thêm danh mục thất bại');
      }
    });
  }
}
