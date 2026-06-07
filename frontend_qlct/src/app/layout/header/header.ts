import { Component, HostListener, ElementRef, OnInit, ViewChild } from '@angular/core';
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
  @ViewChild('menuContainer') menuContainer!: ElementRef;
  @ViewChild('notifContainer') notifContainer!: ElementRef;

  isMenuOpen = false;
  isNotificationsOpen = false;
  notifications: any[] = [];
  isLoggedIn = false;
  user: any = null;
  userId = 0;

  constructor(
    private auth: Auth,
    private notificationService: notificationService
  ) {}

  ngOnInit(): void {
    this.auth.isLoggedIn$.subscribe(status => {
      this.isLoggedIn = status;
      if (status) {
        this.userId = this.auth.getCurrentUserId() ?? 0;
        if (this.userId > 0) this.loadNotifications();
      } else {
        this.notifications = [];
      }
    });

    this.auth.currentUser$.subscribe(userData => this.user = userData);
  }

  loadNotifications(): void {
    if (!this.userId) return;
    this.notificationService.getNotifications(this.userId).subscribe({
      next: (data) => this.notifications = data,
      error: (err) => console.error('Lỗi tải thông báo:', err)
    });
  }

  markAsRead(id: number, event: Event): void {
    event.stopPropagation(); // Cực kỳ quan trọng để không đóng dropdown
    this.notificationService.markAsRead(id).subscribe({
      next: () => {
        this.notifications = this.notifications.filter(n => n.id !== id);
      }
    });
  }

  toggleNotifications(event: Event): void {
    event.stopPropagation();
    this.isNotificationsOpen = !this.isNotificationsOpen;
    this.isMenuOpen = false; // Đóng menu nếu mở thông báo
  }

  toggleMenu(event: Event): void {
    event.stopPropagation();
    this.isMenuOpen = !this.isMenuOpen;
    this.isNotificationsOpen = false; // Đóng thông báo nếu mở menu
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    
    // Đóng nếu click ra ngoài menu
    if (this.menuContainer && !this.menuContainer.nativeElement.contains(target)) {
      this.isMenuOpen = false;
    }
    // Đóng nếu click ra ngoài thông báo
    if (this.notifContainer && !this.notifContainer.nativeElement.contains(target)) {
      this.isNotificationsOpen = false;
    }
  }

  onLogout(): void {
    this.auth.logout();
  }
}