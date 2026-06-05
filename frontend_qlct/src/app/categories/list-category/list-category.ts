import {Component, OnInit, ChangeDetectorRef} from '@angular/core';
import {CategoryService} from '../../services/category-service';
import {Category} from '../../model/category';
import {RouterLink, Router} from '@angular/router';
import {Auth} from '../../services/auth'; // Import Auth service để lấy thông tin đăng nhập

@Component({
  selector: 'app-list-category',
  imports: [RouterLink],
  templateUrl: './list-category.html',
  styleUrl: './list-category.css',
})
export class ListCategory implements OnInit {

  categories: Category[] = [];
  paginatedCategories: Category[] = [];
  userId: number | null = null; // Lưu trữ ID người dùng đang đăng nhập
  
  // Pagination properties
  currentPage: number = 1; // trang hiện tại
  pageSize: number = 3; // kích thước trang
  totalPages: number = 1; // tổng số trang
  pagesArray: number[] = []; // mảng các số trang

  constructor(
    private categoryService: CategoryService,
    private cdr: ChangeDetectorRef,
    private router: Router,
    private auth: Auth // Inject Auth service
  ) {}

  ngOnInit(): void {
    // Lắng nghe thông tin người dùng đăng nhập để lấy userId
    this.auth.currentUser$.subscribe({
      next: (user) => {
        if (user && user.id) {
          this.userId = user.id;
          this.loadCategories(); // Tải danh mục sau khi đã xác định được người dùng
        } else {
          this.userId = null;
          this.router.navigate(['/login']); // Chưa đăng nhập thì chuyển về trang login
        }
      },
      error: (err) => {
        console.error('Error fetching user info in ListCategory:', err);
        this.router.navigate(['/login']);
      }
    });
  }

  loadCategories(): void {
    // Truyền userId vào getAllCategories để lọc danh mục theo người dùng hoặc hệ thống
    this.categoryService.getAllCategories(this.userId || undefined).subscribe({
      next: (data) => {
        this.categories = data.map((item: any) =>
          new Category(item.id, item.name, item.icon, item.type, item.userId)
        );
        this.updatePagination();
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error fetching categories:', err);
      }
    });
  }

  updatePagination(): void {
    this.totalPages = Math.max(1, Math.ceil(this.categories.length / this.pageSize));
    // Đảm bảo trang hiện tại nằm trong giới hạn
    if (this.currentPage > this.totalPages) {
      this.currentPage = this.totalPages;
    } else if (this.currentPage < 1) {
      this.currentPage = 1;
    }
    
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.paginatedCategories = this.categories.slice(startIndex, endIndex);
    
    // Tạo mảng các số trang
    this.pagesArray = Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.updatePagination();
      this.cdr.detectChanges();
    }
  }

  changePageSize(event: Event): void {
    const target = event.target as HTMLSelectElement;
    this.pageSize = parseInt(target.value, 10);
    this.currentPage = 1; // reset to first page
    this.updatePagination();
    this.cdr.detectChanges();
  }

  deleteCategory(id: string): void {
    if (confirm('Bạn có chắc chắn muốn xóa danh mục này?')) {
      this.categoryService.deleteCategory(id).subscribe({
        next: () => {
          this.loadCategories(); // Tải lại danh sách
        },
        error: (err) => {
          console.error('Error deleting category:', err);
        }
      });
    }
  }

  updateCategory(id: string): void {
    this.router.navigate(['/categories/edit', id]);
  }
}
//   deleteCategory(id: string): void {
//   // Xác nhận trước khi xóa
//   if (confirm('Bạn có chắc chắn muốn xóa danh mục này?')) {
//     this.categoryService.deleteCategory(id).subscribe({
//       next: () => {
//         // Xóa thành công, cập nhật lại danh sách
//         this.loadCategories();
//         this.cdr.detectChanges(); // Ép Angular cập nhật
//       },
//       error: (err) => {
//         console.error('Error deleting category:', err);
//       }
//     });
//   }
// }



