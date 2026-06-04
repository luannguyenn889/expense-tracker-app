import { Component, HostListener, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import {Auth} from '../../services/auth';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class AppHeader {
  isMenuOpen = false;

  constructor(private elementRef: ElementRef, private auth :Auth) {}

  toggleMenu() {
    this.isMenuOpen = !this.isMenuOpen;
  }

  // Đóng menu khi click ra ngoài
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const targetElement = event.target as HTMLElement;
    // Kiểm tra xem click có nằm trong khu vực avatar hay không
    if (targetElement && !this.elementRef.nativeElement.querySelector('.topbar__avatar-container').contains(targetElement)) {
      this.isMenuOpen = false;
    }
  }


  //Hien thi menu theo thong tin username password vua login
  isLoggedIn = false;
  user: any = null;



  ngOnInit() {
    // Lắng nghe trạng thái đăng nhập
    this.auth.isLoggedIn$.subscribe(status => {
      this.isLoggedIn = status;
    });

    // Lắng nghe thông tin user để in ra tên/avatar
    this.auth.currentUser$.subscribe(userData => {
      this.user = userData;
    });
  }

  onLogout() {

    this.auth.logout(); // Gọi hàm xóa session trong service
  }
}
