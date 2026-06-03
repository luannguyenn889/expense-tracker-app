export class Category {
  id: string;
  name: string;
  icon: string;
  type: string;
  userId?: number; // ID người dùng sở hữu danh mục này (null nếu là danh mục mặc định của hệ thống)

  constructor(id: string, name: string, icon: string, type: string, userId?: number) {
    this.id = id;
    this.name = name;
    this.icon = icon;
    this.type = type;
    this.userId = userId;
  }
}
