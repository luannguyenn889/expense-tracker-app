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
        // Ép Angular cập nhật giao diện ngay lập tức
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error fetching categories:', err);
      }
    });
  }

  deleteCategory(id: string): void {
  if (confirm('Bạn có chắc chắn muốn xóa danh mục này?')) {
    this.categoryService.deleteCategory(id).subscribe({
      next: () => {
        this.loadCategories(); // Tải lại danh sách
        this.cdr.detectChanges(); // Ép Angular cập nhật giao diện
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
