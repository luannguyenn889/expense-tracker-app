export class Category {
  id: string;
  name: string;
  icon: string;
  type: string; // 'EXPENSE' | 'INCOME'
  userId?: number; // ID người dùng sở hữu danh mục này (null nếu là danh mục mặc định của hệ thống)
  color?: string; // Màu sắc hiển thị của danh mục (hex, ví dụ: #FF5733)
  description?: string; // Mô tả ngắn về danh mục
  monthlyBudget?: number; // Ngân sách tháng (null nếu không giới hạn)

  constructor(
    id: string,
    name: string,
    icon: string,
    type: string,
    userId?: number,
    color?: string,
    description?: string,
    monthlyBudget?: number
  ) {
    this.id = id;
    this.name = name;
    this.icon = icon;
    this.type = type;
    this.userId = userId;
    this.color = color;
    this.description = description;
    this.monthlyBudget = monthlyBudget;
  }
}
