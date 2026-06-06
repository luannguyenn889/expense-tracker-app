import { Component, HostListener, ElementRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

import { Auth } from '../../services/auth';
import { notificationService } from '../../services/notificationService';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class AppHeader implements OnInit {

  // =========================
  // USER MENU
  // =========================
  isMenuOpen = false;

  // =========================
  // NOTIFICATION
  // =========================
  isNotificationsOpen = false;
  notifications: any[] = [];

  // =========================
  // USER INFO
  // =========================
  isLoggedIn = false;
  user: any = null;
  userId = 0;

  constructor(
    private elementRef: ElementRef,
    private auth: Auth,
    private notificationService: notificationService
  ) {}

  ngOnInit(): void {

    // Theo dõi trạng thái đăng nhập
    this.auth.isLoggedIn$.subscribe(status => {
      this.isLoggedIn = status;

      if (status) {
        this.userId = this.auth.getCurrentUserId() ?? 0;

        if (this.userId > 0) {
          this.loadNotifications();
        }
      } else {
        this.notifications = [];
      }
    });

    // Theo dõi thông tin user
    this.auth.currentUser$.subscribe(userData => {
      this.user = userData;
    });
  }

  // =========================
  // NOTIFICATION
  // =========================

   markAsRead(id: number): void {
    this.notificationService.markAsRead(id).subscribe(() => {
      this.loadNotifications();
    });
  }

  toggleNotifications(event?: Event): void {
    if (event) {
      event.stopPropagation();
    }

    this.isNotificationsOpen = !this.isNotificationsOpen;

    // Đóng menu user nếu đang mở
    if (this.isNotificationsOpen) {
      this.isMenuOpen = false;
    }
  }

  // =========================
  // USER MENU
  // =========================

  toggleMenu(event?: Event): void {
    if (event) {
      event.stopPropagation();
    }

    this.isMenuOpen = !this.isMenuOpen;

    // Đóng notification nếu đang mở
    if (this.isMenuOpen) {
      this.isNotificationsOpen = false;
    }
  }

  onLogout(): void {
    this.auth.logout();
  }

  loadNotifications(): void {
  if (!this.userId) return;

  this.notificationService.getNotifications(this.userId).subscribe({
    next: (data: any) => {
      console.log('Notifications:', data);
      this.notifications = data || [];
    },
    error: (err) => {
      console.error('Lỗi tải thông báo:', err);
      this.notifications = [];
    }
  });
}
  // =========================
  // CLICK NGOÀI ĐỂ ĐÓNG
  // =========================

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {

    const target = event.target as HTMLElement;

    const avatarContainer =
      this.elementRef.nativeElement.querySelector(
        '.topbar__avatar-container'
      );

    const notificationContainer =
      this.elementRef.nativeElement.querySelector(
        '.topbar__notifications-container'
      );

    const clickInsideAvatar =
      avatarContainer?.contains(target);

    const clickInsideNotification =
      notificationContainer?.contains(target);

    if (!clickInsideAvatar) {
      this.isMenuOpen = false;
    }

    if (!clickInsideNotification) {
      this.isNotificationsOpen = false;
    }
  }
}