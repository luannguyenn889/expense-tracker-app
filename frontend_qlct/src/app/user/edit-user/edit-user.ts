import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {CommonModule} from '@angular/common';
import {UserService} from '../../services/user-service';
import {User} from '../../model/user';
import {Auth} from '../../services/auth';

@Component({
  selector: 'app-edit-user',
  standalone: true, // Thêm standalone: true
  imports: [FormsModule, RouterLink, CommonModule],
  templateUrl: './edit-user.html',
  styleUrl: './edit-user.css',
})
export class EditUser implements OnInit{

  isLoading: boolean = true;
  user: User = new User();
  userId: number = 0;

  constructor(
    private userService: UserService, // Sửa lại tên biến cho đúng convention
    private router: Router,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef,
    private auth: Auth
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.userId = idParam ? Number(idParam) : (this.auth.getCurrentUserId() || 0);
    if (this.userId) {
      this.userService.getUserById(this.userId).subscribe({
        next: (data) => {
          this.user = data;
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error('Error fetching user:', err);
          this.isLoading = false;
          this.cdr.detectChanges();
        }
      });
    } else {
      this.isLoading = false;
    }
  }

  onSubmit(): void {
    this.isLoading = true;
    this.userService.updateUser(this.userId, this.user).subscribe({
      next: (updatedUser) => {
        console.log('User updated successfully:', updatedUser);
        this.isLoading = false;
        // Cập nhật session lưu thông tin user hiện tại để Header thay đổi avatar/tên ngay lập tức
        this.auth.updateCurrentUser(updatedUser);
        alert('Cập nhật thông tin tài khoản thành công!');
        // Chuyển hướng về trang chủ
        this.router.navigate(['/']);
      },
      error: (err) => {
        console.error('Error updating user:', err);
        this.isLoading = false;
        alert('Cập nhật thất bại!');
      }
    });
  }
}
