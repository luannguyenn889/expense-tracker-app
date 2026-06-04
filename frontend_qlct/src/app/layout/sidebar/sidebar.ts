import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar {
  constructor(private router: Router) {}

  openNewTransaction() {
    sessionStorage.setItem('openAddTransaction', 'true');
    // Chuyển đến trang transactions và mở popup thêm giao dịch
    this.router.navigate(['/transactions']).then(() => {
    });
  }
}
