import {Component, OnInit, ChangeDetectorRef} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {Router, ActivatedRoute, RouterLink} from '@angular/router';
import {CategoryService} from '../../services/category-service';
import {Category} from '../../model/category';
import {CommonModule} from '@angular/common';

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

  constructor(
    private categoryService: CategoryService,
    private router: Router,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.categoryId = this.route.snapshot.paramMap.get('id') || '';
    if (this.categoryId) {
      this.categoryService.getCategoryById(this.categoryId).subscribe({
        next: (data) => {
          this.category = data;
          this.isLoading = false;
          this.cdr.detectChanges(); // Force Angular to update UI immediately
        },
        error: (err) => {
          console.error('Error fetching category:', err);
          this.isLoading = false;
          this.cdr.detectChanges(); // Force UI update on error too
        }
      });
    }
  }

  onSubmit(): void {
    if (!this.category.name || !this.category.icon) {
      alert('Please fill all required fields');
      return;
    }

    this.categoryService.updateCategory(this.categoryId, this.category).subscribe({
      next: () => {
        this.router.navigate(['/list-category']);
      },
      error: (err) => {
        console.error('Error updating category:', err);
        alert('Failed to update category');
      }
    });
  }
}
