import {Component} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {Router, RouterLink} from '@angular/router';
import {CategoryService} from '../../services/category-service';
import {Category} from '../../model/category';

@Component({
  selector: 'app-add-category',
  imports: [FormsModule, RouterLink],
  templateUrl: './add-category.html',
  styleUrl: './add-category.css',
})
export class AddCategory {
  category: Category = new Category('', '', '', 'EXPENSE');

  constructor(
    private categoryService: CategoryService,
    private router: Router
  ) {}

  onSubmit(): void {
    // Basic validation
    if (!this.category.name || !this.category.icon) {
      alert('Please fill all required fields');
      return;
    }

    this.categoryService.addCategory(this.category).subscribe({
      next: () => {
        this.router.navigate(['/list-category']);
      },
      error: (err) => {
        console.error('Error adding category:', err);
        alert('Failed to add category');
      }
    });
  }
}
