import {Component, OnInit, ChangeDetectorRef} from '@angular/core';
import {CategoryService} from '../../services/category-service';
import {Category} from '../../model/category';
import {RouterLink, Router} from '@angular/router';

@Component({
  selector: 'app-list-category',
  imports: [RouterLink],
  templateUrl: './list-category.html',
  styleUrl: './list-category.css',
})
export class ListCategory implements OnInit {

  categories: Category[] = [];
  paginatedCategories: Category[] = [];
  
  // Pagination properties
  currentPage: number = 1;
  pageSize: number = 5;
  totalPages: number = 1;
  pagesArray: number[] = [];

  constructor(
    private categoryService: CategoryService,
    private cdr: ChangeDetectorRef,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.categoryService.getAllCategories().subscribe({
      next: (data) => {
        this.categories = data.map((item: any) =>
          new Category(item.id, item.name, item.icon, item.type)
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
    
    // Ensure currentPage is within bounds
    if (this.currentPage > this.totalPages) {
      this.currentPage = this.totalPages;
    } else if (this.currentPage < 1) {
      this.currentPage = 1;
    }
    
    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    this.paginatedCategories = this.categories.slice(startIndex, endIndex);
    
    // Generate page numbers array
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


}
