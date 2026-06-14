import { Component, OnInit,ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SavingGoalService, SavingGoalRequest } from '../../services/saving-goal-service';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-saving-goals',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './saving-goals.html',
  styleUrl: './saving-goals.css'
})
export class SavingGoals implements OnInit {
  userId: number = 0;

  goalName: string = '';
  targetAmount: number | null = null;
  targetDate: string = '';

  isLoading: boolean = false;
  errorMessage: string = '';
  successMessage: string = '';

  showModal: boolean = false;
  selectedGoal: any = null;
  wallets: any[] = [];
  selectedWalletId: number | null = null;
  contributeAmount: number | null = null;
  modalError: string = '';
  isContributing: boolean = false;
  savingGoalsList: any[] = [];

  // --- CÁC BIẾN CHO SỬA / XÓA ---
  showEditModal: boolean = false;
  editGoalData: any = {};
  isEditing: boolean = false;

  showDeleteModal: boolean = false;
  goalToDelete: any = null;
  refundWalletId: number | null = null;
  isDeleting: boolean = false;

  constructor(
    private savingGoalService: SavingGoalService,
    private http: HttpClient, // Inject thêm HttpClient
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const storedUserId = sessionStorage.getItem('userId') || localStorage.getItem('userId');
    this.userId = storedUserId ? Number(storedUserId) : 7; // Dùng ID 7 như bạn đang test

    this.setDefaultDate();
    this.loadWallets();
    this.loadGoals();
  }

  setDefaultDate(): void {
    // Set mặc định ngày hoàn thành là ngày mai
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    this.targetDate = tomorrow.toISOString().split('T')[0];
  }

  onSubmit(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.goalName || !this.targetAmount || !this.targetDate) {
      this.errorMessage = 'Vui lòng điền đầy đủ thông tin.';
      return;
    }

    const request: SavingGoalRequest = {
      name: this.goalName,
      targetAmount: this.targetAmount,
      targetDate: this.targetDate
    };

    this.isLoading = true;
    this.savingGoalService.addSavingGoal(request, this.userId).subscribe({
      next: (res) => {
        this.isLoading = false;
        this.successMessage = 'Tạo mục tiêu tiết kiệm thành công!';

        // Reset form
        this.goalName = '';
        this.targetAmount = null;
        this.setDefaultDate();
        this.loadGoals();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error || 'Đã xảy ra lỗi khi tạo mục tiêu.';
        this.cdr.detectChanges();
      }
    });
  }

  // Lấy danh sách ví theo userId
  loadWallets(): void {
    this.http.get<any[]>(`http://localhost:8080/api/wallets/all?userId=${this.userId}`).subscribe({
      next: (data) => this.wallets = data,
      error: (err) => console.error('Lỗi tải ví', err)
    });
  }

  // --- CÁC HÀM ĐIỀU KHIỂN MODAL ---
  openModal(goal: any): void {
    this.selectedGoal = goal;
    this.showModal = true;
    this.selectedWalletId = null;
    this.contributeAmount = null;
    this.modalError = '';
  }

  closeModal(): void {
    if (this.isContributing) return;
    this.showModal = false;
    this.selectedGoal = null;
  }

  onContributeSubmit(): void {
    this.modalError = '';

    if (!this.selectedWalletId || !this.contributeAmount || this.contributeAmount <= 0) {
      this.modalError = 'Vui lòng chọn ví và nhập số tiền hợp lệ (> 0).';
      return;
    }

    const request = {
      goalId: this.selectedGoal.id,
      walletId: this.selectedWalletId,
      amount: this.contributeAmount
    };

    this.isContributing = true;
    this.savingGoalService.contributeToGoal(request, this.userId).subscribe({
      next: (res) => {
        this.isContributing = false;

        // 1. TẠO CÂU THÔNG BÁO TRƯỚC KHI ĐÓNG MODAL
        this.successMessage = `Nạp thành công ${this.contributeAmount}đ vào quỹ ${this.selectedGoal.name}!`;

        // 2. SAU ĐÓ MỚI ĐÓNG MODAL (để không bị lỗi mất dữ liệu selectedGoal)
        this.closeModal();

        // 3. TẢI LẠI DANH SÁCH & ÉP UI CẬP NHẬT
        this.loadGoals();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isContributing = false;
        this.modalError = err.error?.message || (typeof err.error === 'string' ? err.error : 'Nạp tiền thất bại từ Backend!');
        this.cdr.detectChanges();
      }
    });
  }

  // --- HÀM SỬA (UC24) ---
  openEditModal(goal: any): void {
    this.editGoalData = { ...goal }; // Clone dữ liệu để không ảnh hưởng list chính
    this.showEditModal = true;
    this.modalError = '';
  }

  closeEditModal(): void {
    if (this.isEditing) return;
    this.showEditModal = false;
  }

  onEditSubmit(): void {
    this.modalError = '';
    const request: SavingGoalRequest = {
      name: this.editGoalData.name,
      targetAmount: this.editGoalData.targetAmount,
      targetDate: this.editGoalData.targetDate
    };

    this.isEditing = true;
    this.savingGoalService.updateSavingGoal(this.editGoalData.id, request).subscribe({
      next: () => {
        this.isEditing = false;
        this.closeEditModal();
        this.successMessage = 'Cập nhật mục tiêu thành công!';
        this.loadGoals();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isEditing = false;
        this.modalError = err.error?.message || (typeof err.error === 'string' ? err.error : 'Lỗi cập nhật.');
        this.cdr.detectChanges();
      }
    });
  }

  // --- HÀM XÓA (UC25) ---
  openDeleteModal(goal: any): void {
    this.goalToDelete = goal;
    this.refundWalletId = null;
    this.showDeleteModal = true;
    this.modalError = '';
  }

  closeDeleteModal(): void {
    if (this.isDeleting) return;
    this.showDeleteModal = false;
    this.goalToDelete = null;
  }

  onDeleteSubmit(): void {
    this.modalError = '';
    this.isDeleting = true;

    this.savingGoalService.deleteSavingGoal(this.goalToDelete.id, this.refundWalletId, this.userId).subscribe({
      next: () => {
        this.isDeleting = false;

        // 1. LẤY TÊN QUỸ ĐỂ BÁO THÀNH CÔNG TRƯỚC
        this.successMessage = `Đã xóa quỹ "${this.goalToDelete.name}" thành công!`;

        // 2. SAU ĐÓ MỚI ĐÓNG MODAL (để tránh bị null)
        this.closeDeleteModal();

        // 3. TẢI LẠI DANH SÁCH & CẬP NHẬT UI
        this.loadGoals();
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.isDeleting = false;
        this.modalError = err.error?.message || (typeof err.error === 'string' ? err.error : 'Lỗi khi xóa.');
        this.cdr.detectChanges();
      }
    });
  }

  loadGoals(): void {
    this.savingGoalService.getSavingGoals(this.userId).subscribe({
      next: (data) => {
        this.savingGoalsList = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Lỗi tải danh sách mục tiêu', err)
    });
  }

  // HÀM TÍNH TRẠNG THÁI CHO [UC23]
  getGoalStatus(goal: any): { text: string; class: string } {
    // 1. Nếu đã gom đủ hoặc vượt tiền mục tiêu
    if (goal.currentAmount >= goal.targetAmount) {
      return { text: 'Đã hoàn thành', class: 'badge-success' };
    }

    // 2. So sánh ngày hạn với ngày hôm nay
    const today = new Date();
    today.setHours(0, 0, 0, 0); // Đưa về 0h để so sánh chính xác ngày
    const targetDate = new Date(goal.targetDate);
    targetDate.setHours(0, 0, 0, 0);

    // Nếu chưa đủ tiền mà ngày hiện tại đã vượt quá ngày hạn
    if (today > targetDate) {
      return { text: 'Quá hạn', class: 'badge-danger' };
    }

    // 3. Trường hợp còn lại
    return { text: 'Đang thực hiện', class: 'badge-info' };
  }
}
